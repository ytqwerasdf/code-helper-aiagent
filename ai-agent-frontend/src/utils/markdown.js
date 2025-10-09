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
  return marked.parse(text)
}


