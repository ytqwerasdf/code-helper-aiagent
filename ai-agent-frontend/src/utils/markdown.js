import { marked } from 'marked'

// 配置 marked（可按需扩展）
marked.setOptions({
  breaks: true,
  gfm: true
})

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
  
  return marked.parse(processedText)
}


