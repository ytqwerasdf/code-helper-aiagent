let toastTimer = null

/**
 * 显示全局轻提示
 * @param {string} message 提示文本
 * @param {number} duration 显示时长ms
 */
export function showToast(message, duration = 2000) {
  // 清除之前的定时器
  if (toastTimer) {
    clearTimeout(toastTimer)
    toastTimer = null
  }

  let el = document.getElementById('global-toast')
  if (!el) {
    el = document.createElement('div')
    el.id = 'global-toast'
    el.className = 'toast'
    document.body.appendChild(el)
  }
  
  el.textContent = message || ''
  el.className = 'toast show'

  // 设置定时器隐藏Toast
  toastTimer = setTimeout(() => {
    if (el) {
      el.className = 'toast'
    }
    toastTimer = null
  }, duration)
}


