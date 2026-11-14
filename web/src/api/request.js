import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const body = response.data
    // 后端统一返回 { code, message, data }
    if (body && body.code !== 200) {
      ElMessage.error(body.message || '请求失败')
      if (body.code === 401) {
        localStorage.clear()
        window.location.href = '/login'
      }
      return Promise.reject(new Error(body.message))
    }
    return body.data
  },
  (error) => {
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request
