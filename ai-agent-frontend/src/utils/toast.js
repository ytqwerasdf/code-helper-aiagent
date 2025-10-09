let toastTimer = null

/**
 * 显示全局轻提示
 * @param {string} message 提示文本
 * @param {number} duration 显示时长ms
 */
export function showToast(message, duration = 1500) {
  let el = document.getElementById('global-toast')
  if (!el) {
    el = document.createElement('div')
    el.id = 'global-toast'
    document.body.appendChild(el)
  }
  el.textContent = message || ''
  el.className = 'toast show'

  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    el.className = 'toast'
  }, duration)
}


