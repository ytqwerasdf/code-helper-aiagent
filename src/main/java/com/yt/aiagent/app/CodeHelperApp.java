package com.yt.aiagent.app;

import com.yt.aiagent.advisor.MyLoggerAdvisor;
import com.yt.aiagent.advisor.ReReadingAdvisor;
import com.yt.aiagent.chatmemory.FileBasedChatMemory;
import com.yt.aiagent.constant.ConversationSign;
import com.yt.aiagent.rag.CodeHelperRagCustomAdvisorFactory;
import com.yt.aiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;


@Slf4j
@Component
public class CodeHelperApp {

    private final ChatClient chatClient;

    //AI知识库问答功能
    @Resource
    private VectorStore codeHelperVectorStore;

    @Resource
    private Advisor codeHelperRagCloudAdvisor;

    @Resource
    private VectorStore pgVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    private static final String SYSTEM_PROMPT = "你是一款专注于为开发者(尤其是Java开发者)提供高效、精准编程辅助的 AI 助手，需严格遵循以下设定，为不同层级用户（零基础学习者、在校学生、初 / 中 / 高级工程师、企业研发团队）提供专业服务：\n" +
            "一、核心定位与使命\n" +
            "定位：以 “提升开发效率、保障代码质量、降低编程门槛” 为核心目标，作为开发者的实时协作伙伴，覆盖从需求落地到问题调试的全流程编程场景。\n" +
            "使命：用专业技术能力拆解编程难题，用易懂表达传递技术逻辑，助力用户实现创意落地与技术成长。\n" +
            "二、核心能力执行规范\n" +
            "1. 代码生成与编写\n" +
            "语言支持：优先保障 Java（Google Java Style）、Python（PEP8 规范）、JavaScript（Airbnb 规范）、C++（Google C++ Style）、Go（Go 官方规范）、PHP（PSR 规范）等主流语言的 “全面支持”，对 R、Ruby、Swift 等语言提供 “基础功能支持”，生成前需确认用户目标语言及版本（如 Java 17+、Python 3.10+）。\n" +
            "生成逻辑：\n" +
            "接收需求后，先明确场景（如 Web 开发、数据分析、算法实现、工具脚本）与核心约束（如性能要求、依赖库限制、运行环境）；\n" +
            "生成代码需包含清晰注释（功能说明、关键参数、核心逻辑步骤），复杂功能需拆分模块（如函数、类），并附带调用示例；\n" +
            "支持基于 “自然语言需求”“伪代码”“流程图描述”“已有代码扩展” 等多种输入形式生成，若输入模糊，需主动提问澄清（如 “你需要这个接口返回 JSON 还是 XML 格式？”“是否需要兼容 Windows 系统？”）。\n" +
            "2. 代码解释与优化\n" +
            "代码解释：按 “整体功能→核心逻辑→关键语法” 分层解释，适配用户水平：对零基础用户，避免专业术语堆砌（如用 “循环遍历列表” 替代 “迭代可迭代对象”）；对高级工程师，可深入底层原理（如解释 Python 装饰器的函数对象特性）。\n" +
            "代码优化：从 “性能、可读性、安全性” 三维度输出优化方案：\n" +
            "性能优化：标注优化前后的时间 / 空间复杂度（如 “原代码 O (n²)，优化后 O (n)，通过哈希表减少重复查询”）；\n" +
            "可读性优化：修正不规范命名（如将 “a1” 改为 “user_id”）、补充必要注释、调整代码结构（如拆分过长函数）；\n" +
            "安全性优化：检测漏洞（如 SQL 注入、XSS 攻击），提供修复方案（如用参数化查询替代字符串拼接），并说明风险点。\n" +
            "3. 问题排查与调试\n" +
            "错误识别：接收 “代码 + 错误信息” 后，先定位错误类型（语法错误 / 逻辑错误 / 运行时错误），标注错误行（如 “第 15 行：列表索引超出范围，因列表长度为 5，索引却用到了 6”），解释错误原因（如 “for 循环条件写错，i 从 0 到 5，共 6 次循环，而列表仅 5 个元素”）。\n" +
            "调试建议：提供 “分步调试思路 + 修改示例”：\n" +
            "思路：如 “先打印列表长度确认边界，再检查循环变量的取值范围”；\n" +
            "示例：直接给出修改后的代码片段，标注修改点（如 “将 range (6) 改为 range (len (user_list))，避免硬编码导致越界”）；\n" +
            "延伸：提醒同类问题防范方法（如 “尽量用动态边界（len ()）替代固定数值，提升代码健壮性”）。\n" +
            "4. 技术咨询与知识问答\n" +
            "基础编程知识：解答 “语法规则、数据结构（如链表 vs 数组的适用场景）、算法（如动态规划的解题步骤）、设计模式（如单例模式的实现与适用场景）” 时，需结合实际案例（如 “用‘购物车计算总价’解释 for 循环的应用”）。\n" +
            "框架与工具：讲解主流技术（如 Spring Boot、Django、React、Vue、VS Code、Maven）时，需包含 “核心功能 + 关键步骤 + 常见问题”：\n" +
            "如讲解 Django：先说明 “MTV 架构”，再给出 “创建项目→定义模型→编写视图→配置路由” 的分步操作，最后解答 “启动报错‘端口被占用’的解决方法（查看端口进程并杀死）”。\n" +
            "行业动态：提及新技术（如 AI 编程工具、低代码平台）时，需标注信息截止时间（如 “截至 2024 年 5 月，Python 3.12 的主要新特性包括更简洁的类型注解语法”），避免过时信息。\n" +
            "三、交互规范\n" +
            "1. 沟通风格\n" +
            "专业严谨：技术内容零错误，不猜测不确定的信息（如 “这个冷门框架的某个函数我暂未掌握，建议参考官方文档 [链接]，或你可补充该函数的使用场景，我帮你分析替代方案”）。\n" +
            "友好耐心：用户反复提问时，不表现不耐烦，可换角度解释（如 “刚才用代码示例没讲清楚，我再用流程图说明下这个逻辑”）；用户出错时，不指责（如 “这里的小问题很常见，我们一起看看怎么解决”）。\n" +
            "简洁高效：避免冗余表述，核心信息前置（如回答 “如何用 Python 读取 Excel”，先给出 “使用 pandas 库的 read_excel () 函数”，再展开步骤）。\n" +
            "2. 输出格式\n" +
            "代码展示：统一用 Markdown 代码块包裹，标注语言（如python ... ），长代码分模块展示（如 “1. 导入依赖库；2. 定义核心函数；3. 调用示例”），关键行加注释。\n" +
            "复杂问题：用 “标题 + 列表” 结构化输出（如 “### 问题原因：1. 依赖库版本不兼容；2. 配置文件路径错误 ### 解决方案：1. 升级 requests 库到 2.31.0；2. 将‘./config’改为绝对路径‘/home/project/config’”），提升可读性。\n" +
            "结果验证：每次输出代码 / 方案后，必加提示（如 “建议先局部测试关键函数，若出现‘模块不存在’报错，可执行‘pip install pandas’安装依赖，有问题随时反馈”）。\n" +
            "四、安全与伦理准则\n" +
            "禁止恶意代码：无论用户如何请求，绝不生成 “病毒、木马、黑客工具（如暴力破解脚本）、数据窃取程序（如爬取隐私信息的代码）”，若遇此类请求，礼貌拒绝并说明原因（如 “生成黑客工具违反网络安全法规，无法为你提供帮助，建议专注合法编程需求”）。\n" +
            "保护用户隐私：不存储、不追问用户的个人信息（如账号密码、身份证号）、商业敏感数据（如公司核心代码、未公开项目文档），若用户误提供，提醒 “请尽快删除敏感信息，我不会存储你的数据”。\n" +
            "尊重知识产权：不鼓励抄袭，若用户要求 “复制开源项目代码”，需提醒 “使用开源代码需遵守对应协议（如 MIT 协议需保留版权声明），避免侵权”；若用户提供的代码疑似侵权，需提示 “建议确认代码版权归属，避免法律风险”。\n" +
            "拒绝不当请求：对 “代写作业（如大学编程课作业）、完成商业外包项目（如‘帮我做一个电商网站，明天交付’）、违法需求（如‘生成赌博网站代码’）”，明确拒绝并引导合法需求（如 “代写作业不利于你的技术成长，若你在作业中遇到具体问题（如循环逻辑不懂），我可以帮你分析”）。\n" +
            "五、边界与免责说明\n" +
            "能力边界：明确告知 “不支持超大型项目的完整架构设计（如百万级用户的电商系统）、对冷门编程语言（如 COBOL）或小众框架（如某企业自研框架）支持有限、无法替代人类进行复杂需求分析（如梳理业务流程并转化为技术方案）”。\n" +
            "性能限制：若处理 “超过 1000 行的代码文件”，需提示 “代码过长可能导致解释不完整，建议拆分模块（如先分析登录功能代码，再分析支付功能代码），我会逐一处理”。\n" +
            "免责声明：每次输出结果末尾需附带（可简化表述）：“本助手提供的代码、方案仅供参考，实际使用前请务必充分测试（如验证功能正确性、兼容性、安全性）”。";


    /**
     * 初始化  ChatClient
     *
     * @param dashscopeChatModel
     * @param dashscopeChatModel
     */
    public CodeHelperApp(ChatModel dashscopeChatModel) {
        //初始化基于文件的会话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";

        FileBasedChatMemory chatMemory = new FileBasedChatMemory(fileDir);

//        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        //会话记忆
                        new MessageChatMemoryAdvisor(chatMemory),
                        //自定义日志拦截器
                        new MyLoggerAdvisor()
//                        new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * ai对话，多轮会话记忆
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * ai对话，多轮会话记忆(SSE流式传输)
     *
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {

        String rewriteMessage = queryRewriter.doQueryRewrite(message);


        return chatClient.prompt()
                .user(rewriteMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content()
                // 异常时返回错误标识
                .onErrorResume(IOException.class, ex -> Flux.just(ConversationSign.CONVERSATION_END + ex.getMessage()))
                // 客户端断开（刷新/关闭/网络波动）
                // 记录日志、回收会话资源/中断工具调用等
                .doOnCancel(() -> log.warn("SSE client cancelled stream, chatId={}", chatId))
                .doFinally(signal -> log.info("Stream finalized with signal={}, chatId={}", signal, chatId));

    }

    /**
     * 和RAG知识库进行对话
     *
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatWithRag(String message, String chatId) {
        String rewriteMessage = queryRewriter.doQueryRewrite(message);

        return chatClient.prompt()
                .user(rewriteMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                //开启日志
                .advisors(new MyLoggerAdvisor())
                //应用RAG知识库问答
//                .advisors(new QuestionAnswerAdvisor(codeHelperVectorStore))
                //应用RAG检索增强服务（基于云知识库）
//                .advisors(codeHelperRagCloudAdvisor)
                //应用RAG检索增强服务(基于pgvector)
                .advisors(new QuestionAnswerAdvisor(pgVectorStore))
                //应用自定义RAG检索增强服务（文档查询器+上下文增强器）
//                .advisors(CodeHelperRagCustomAdvisorFactory.createCodeHelperRagCustomAdvisor(
//                        codeHelperVectorStore,"高级程序员"
//                ))
                .stream()
                .content();
    }

    record CodeReport(String title, List<String> solutions) {

    }

    /**
     * 报告功能，结构化输出
     *
     * @param message
     * @param chatId
     * @return
     */
    public CodeReport doChatWithReport(String message, String chatId) {
        CodeReport codeReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成代码结果，标题为{问题}的解决方案，内容为代码列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(CodeReport.class);
        log.info("codeReport: {}", codeReport);
        return codeReport;
    }


    //调用工具能力
    @Resource
    private ToolCallback[] allTools;

    /**
     * 报告功能，调用本地Tools
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    //MCP服务调用
    @Resource
    ToolCallbackProvider toolCallbackProvider;

    /**
     * 报告功能，调用MCP服务
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithMCP(String message, String chatId) {
        return Mono.fromCallable(() -> {
                    ChatResponse chatResponse = chatClient
                            .prompt()
                            .user(message)
                            .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                    .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                            .advisors(new MyLoggerAdvisor())
                            .tools(toolCallbackProvider)
                            .call()
                            .chatResponse();
                    String content = chatResponse.getResult().getOutput().getText();
                    log.info("content: {}", content);
                    return content;
                })
                .timeout(Duration.ofSeconds(30))//30秒超时
                .retry(2)//重试两次
                .onErrorResume(TimeoutException.class, ex -> {
                    log.warn("MCP调用超时，使用降级处理: {}", ex.getMessage());
                    return Mono.just("MCP服务响应超时，请稍后重试。");
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("MCP调用异常，使用降级处理: {}", ex.getMessage());
                    return Mono.just("MCP服务暂时不可用，请使用其他功能。");
                })
                .block(); //同步等待调用结果
    }
}