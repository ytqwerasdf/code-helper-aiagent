import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import CodeHelper from '../views/CodeHelper.vue'
import ManusAgent from '../views/ManusAgent.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/code-helper',
    name: 'CodeHelper',
    component: CodeHelper
  },
  {
    path: '/manus-agent',
    name: 'ManusAgent',
    component: ManusAgent
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

