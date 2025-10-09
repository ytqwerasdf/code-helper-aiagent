/**
 * 复制文本到剪贴板
 * @param {string} text 文本
 * @returns {Promise<void>}
 */
export async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text || '')
  } catch (_) {
    const textarea = document.createElement('textarea')
    textarea.value = text || ''
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
  }
}


