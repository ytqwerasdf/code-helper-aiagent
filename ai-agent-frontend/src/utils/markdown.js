import { marked } from 'marked'

// 配置 marked（可按需扩展）
marked.setOptions({
  breaks: true,        // 单个换行符也转换为 <br>
  gfm: true,           // 启用 GitHub Flavored Markdown（支持表格、任务列表等）
  pedantic: false,     // 不启用严格的 markdown.pl 兼容模式
  sanitize: false,     // 不清理 HTML（允许 HTML 标签）
  smartLists: true,    // 使用智能列表
  smartypants: false,  // 不转换引号和破折号
  headerIds: false,    // 不自动生成标题 ID
  mangle: false        // 不混淆邮箱地址
})

/**
 * 检测文本是否包含中英文混合内容
 * @param {string} text 输入文本
 * @returns {boolean} 是否包含中英文混合
 */
function hasMixedLanguages(text) {
  if (!text) return false
  
  // 检测中文字符
  const chineseRegex = /[\u4e00-\u9fff]/
  // 检测英文字符
  const englishRegex = /[a-zA-Z]/
  
  return chineseRegex.test(text) && englishRegex.test(text)
}

/**
 * 保护URL，避免被中英文分离功能误处理
 * @param {string} text 输入文本
 * @returns {object} 包含保护后文本和URL映射的对象
 */
function protectUrls(text) {
  const urlRegex = /(https?:\/\/[^\s<>"{}|\\^`\[\]()]+)/g
  const urlMap = new Map()
  let protectedText = text
  let urlIndex = 0
  
  // 将所有URL替换为占位符
  protectedText = protectedText.replace(urlRegex, (url) => {
    const placeholder = `__URL_PLACEHOLDER_${urlIndex}__`
    urlMap.set(placeholder, url)
    urlIndex++
    return placeholder
  })
  
  return { protectedText, urlMap }
}

/**
 * 恢复被保护的URL
 * @param {string} text 包含占位符的文本
 * @param {Map} urlMap URL映射表
 * @returns {string} 恢复URL后的文本
 */
function restoreUrls(text, urlMap) {
  let restoredText = text
  for (const [placeholder, url] of urlMap) {
    restoredText = restoredText.replace(new RegExp(placeholder, 'g'), url)
  }
  return restoredText
}

/**
 * 将中英文内容分离
 * @param {string} text 输入文本
 * @returns {string} 分离后的HTML
 */
function separateChineseEnglish(text) {
  if (!text) return ''
  
  // 先保护URL，避免被分割
  const { protectedText, urlMap } = protectUrls(text)
  
  // 按段落分割
  const paragraphs = protectedText.split(/\n\s*\n/)
  const separatedParagraphs = []
  
  for (const paragraph of paragraphs) {
    if (!paragraph.trim()) {
      separatedParagraphs.push('')
      continue
    }
    
    // 检测当前段落是否包含中英文混合
    if (hasMixedLanguages(paragraph)) {
      // 更智能的句子分割，考虑更多标点符号
      const sentences = paragraph.split(/([.!?。！？;；]+\s*)/)
      const chineseSentences = []
      const englishSentences = []
      
      for (let i = 0; i < sentences.length; i += 2) {
        const sentence = sentences[i]
        const punctuation = sentences[i + 1] || ''
        const fullSentence = sentence + punctuation
        
        if (!sentence.trim()) continue
        
        // 检测句子语言
        const chineseRegex = /[\u4e00-\u9fff]/
        const englishRegex = /[a-zA-Z]/
        
        // 计算中英文字符比例
        const chineseCount = (sentence.match(/[\u4e00-\u9fff]/g) || []).length
        const englishCount = (sentence.match(/[a-zA-Z]/g) || []).length
        
        if (chineseCount > englishCount) {
          chineseSentences.push(fullSentence.trim())
        } else if (englishCount > chineseCount) {
          englishSentences.push(fullSentence.trim())
        } else if (chineseRegex.test(sentence)) {
          // 如果比例相等但有中文字符，归入中文
          chineseSentences.push(fullSentence.trim())
        } else if (englishRegex.test(sentence)) {
          // 如果比例相等但有英文字符，归入英文
          englishSentences.push(fullSentence.trim())
        } else {
          // 如果既不是中文也不是英文，归入中文部分
          chineseSentences.push(fullSentence.trim())
        }
      }
      
      // 构建分离后的HTML - 简洁版本
      let separatedContent = ''
      
      if (chineseSentences.length > 0) {
        separatedContent += `<div class="chinese-text">${chineseSentences.join(' ')}</div>`
      }
      
      if (englishSentences.length > 0) {
        separatedContent += `<div class="english-text">${englishSentences.join(' ')}</div>`
      }
      
      separatedParagraphs.push(separatedContent)
    } else {
      // 纯中文或纯英文，直接添加
      separatedParagraphs.push(paragraph)
    }
  }
  
  // 恢复URL
  const result = separatedParagraphs.join('\n\n')
  return restoreUrls(result, urlMap)
}

/**
 * 自动将URL转换为markdown链接格式
 * @param {string} text 输入文本
 * @returns {string} 转换后的文本
 */
function autoLinkUrls(text) {
  if (!text) return ''
  
  // 更精确的URL正则表达式，匹配http/https协议
  const urlRegex = /(https?:\/\/[^\s<>"{}|\\^`\[\]()]+)/g
  
  return text.replace(urlRegex, (url) => {
    // 清理URL末尾的标点符号
    url = url.replace(/[.,;:!?]+$/, '')
    
    // 如果URL已经包含在markdown链接中，则不处理
    if (text.includes(`[${url}](${url})`) || text.includes(`](${url})`)) {
      return url
    }
    
    // 生成链接文本，如果URL太长则截断
    let linkText = url
    if (url.length > 60) {
      // 尝试保留域名部分
      try {
        const urlObj = new URL(url)
        const domain = urlObj.hostname
        if (domain.length < 30) {
          linkText = `${domain}...`
        } else {
          linkText = url.substring(0, 57) + '...'
        }
      } catch (e) {
        linkText = url.substring(0, 57) + '...'
      }
    }
    
    return `[${linkText}](${url})`
  })
}

/**
 * 在HTML中直接处理URL链接
 * @param {string} html 输入HTML
 * @returns {string} 处理后的HTML
 */
function processUrlsInHtml(html) {
  if (!html) return ''
  
  // 匹配不在链接标签内的URL
  const urlRegex = /(?!<a[^>]*>)(https?:\/\/[^\s<>"{}|\\^`\[\]()]+)(?![^<]*<\/a>)/g
  
  return html.replace(urlRegex, (url) => {
    // 清理URL末尾的标点符号
    url = url.replace(/[.,;:!?]+$/, '')
    
    // 生成链接文本
    let linkText = url
    if (url.length > 60) {
      try {
        const urlObj = new URL(url)
        const domain = urlObj.hostname
        if (domain.length < 30) {
          linkText = `${domain}...`
        } else {
          linkText = url.substring(0, 57) + '...'
        }
      } catch (e) {
        linkText = url.substring(0, 57) + '...'
      }
    }
    
    return `<a href="${url}" target="_blank" style="color: #0066cc; text-decoration: none; border-bottom: 1px solid transparent; transition: all 0.3s ease;" onmouseover="this.style.borderBottomColor='#0066cc'" onmouseout="this.style.borderBottomColor='transparent'">${linkText}</a>`
  })
}

/**
 * 修复流式渲染时的不完整 markdown 语法
 * @param {string} text 输入文本
 * @returns {string} 修复后的文本
 */
function fixIncompleteMarkdown(text) {
  if (!text) return ''
  
  let fixed = text
  
  // 修复未闭合的代码块（流式渲染时可能出现）
  // 注意：需要排除代码块内部的内容
  const codeBlockRegex = /```[\s\S]*?```/g
  const codeBlockMatches = fixed.match(codeBlockRegex)
  const codeBlockCount = codeBlockMatches ? codeBlockMatches.length : 0
  const allCodeBlockMarkers = (fixed.match(/```/g) || []).length
  
  // 如果代码块标记数量是奇数，说明有未闭合的代码块
  if (allCodeBlockMarkers % 2 !== 0 && !fixed.trim().endsWith('```')) {
    // 检查最后是否在代码块中（简单检查）
    const lastCodeBlockIndex = fixed.lastIndexOf('```')
    const afterLastMarker = fixed.substring(lastCodeBlockIndex + 3)
    // 如果后面没有另一个 ```，说明未闭合
    if (!afterLastMarker.includes('```')) {
      fixed += '\n```'
    }
  }
  
  // 修复未闭合的行内代码标记（`）
  // 需要排除代码块中的内容
  const tempText = fixed.replace(codeBlockRegex, '')
  const inlineCodeMatches = (tempText.match(/`/g) || []).length
  if (inlineCodeMatches % 2 !== 0 && !fixed.trim().endsWith('`')) {
    // 检查是否在行内代码中
    const lastInlineIndex = tempText.lastIndexOf('`')
    const afterLastInline = tempText.substring(lastInlineIndex + 1)
    if (!afterLastInline.includes('`')) {
      fixed += '`'
    }
  }
  
  return fixed
}

/**
 * 将纯文本转为安全的 HTML（Markdown 渲染）
 * @param {string} text 输入 Markdown 文本
 * @returns {string} 渲染后的 HTML
 */
export function renderMarkdown(text) {
  if (!text) return ''
  
  try {
    // 预处理：将 \n 转换为实际的换行符
    let processedText = text.replace(/\\n/g, '\n')
    
    // 处理双换行符，确保段落分隔
    processedText = processedText.replace(/\n\n/g, '\n\n')
    
    // 修复流式渲染时可能的不完整 markdown 语法
    processedText = fixIncompleteMarkdown(processedText)
    
    // 自动将URL转换为markdown链接
    processedText = autoLinkUrls(processedText)
    
    // 先渲染Markdown，保持markdown语法完整性
    let html = marked.parse(processedText)
    
    // 在HTML中再次处理URL，确保所有URL都被转换为链接
    html = processUrlsInHtml(html)
    
    // 注意：中英文分离功能暂时禁用，因为它会破坏markdown语法
    // 如果需要中英文分离，应该在markdown渲染后进行，但需要更复杂的HTML解析
    // const separatedText = separateChineseEnglish(processedText)
    
    return html
  } catch (error) {
    // 如果 markdown 解析失败，返回转义的纯文本
    console.error('Markdown 渲染失败:', error)
    // 转义 HTML 特殊字符，避免 XSS
    const escaped = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')
      .replace(/\n/g, '<br>')
    return escaped
  }
}


