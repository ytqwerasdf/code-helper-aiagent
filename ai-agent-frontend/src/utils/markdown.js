import { marked } from 'marked'

// 配置 marked（可按需扩展）
marked.setOptions({
  breaks: true,
  gfm: true
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
 * 将中英文内容分离
 * @param {string} text 输入文本
 * @returns {string} 分离后的HTML
 */
function separateChineseEnglish(text) {
  if (!text) return ''
  
  // 按段落分割
  const paragraphs = text.split(/\n\s*\n/)
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
  
  return separatedParagraphs.join('\n\n')
}

/**
 * 将纯文本转为安全的 HTML（Markdown 渲染）
 * @param {string} text 输入 Markdown 文本
 * @returns {string} 渲染后的 HTML
 */
export function renderMarkdown(text) {
  if (!text) return ''
  
  // 预处理：将 \n 转换为实际的换行符
  let processedText = text.replace(/\\n/g, '\n')
  
  // 处理双换行符，确保段落分隔
  processedText = processedText.replace(/\n\n/g, '\n\n')
  
  // 检测并分离中英文内容
  const separatedText = separateChineseEnglish(processedText)
  
  // 渲染Markdown
  return marked.parse(separatedText)
}


