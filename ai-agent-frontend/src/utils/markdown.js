import { marked } from 'marked'

// 配置 marked（可按需扩展）
marked.setOptions({
  breaks: true,
  gfm: true,
  // 启用自动链接功能
  pedantic: false,
  sanitize: false,
  smartLists: true,
  smartypants: false
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
  
  // 自动将URL转换为markdown链接
  processedText = autoLinkUrls(processedText)
  
  // 检测并分离中英文内容
  const separatedText = separateChineseEnglish(processedText)
  
  // 渲染Markdown
  let html = marked.parse(separatedText)
  
  // 在HTML中再次处理URL，确保所有URL都被转换为链接
  html = processUrlsInHtml(html)
  
  return html
}


