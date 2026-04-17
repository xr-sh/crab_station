# 修复前端认证状态持久化问题

## TL;DR

> **问题**: 页面刷新时显示"您未获授权"，因为 Zustand persist 恢复状态需要时间，PrivateRoute 立即检查导致误判未登录。
> 
> **解决方案**: 添加 hydration 状态跟踪，PrivateRoute 等待恢复完成后再判断。
> 
> **修改文件**: 2个（authStore.ts、PrivateRoute/index.tsx）

## Context

### 问题现象
- 用户登录后访问 `/api-config` 页面
- 刷新页面后显示"您未获授权，无法查看此网页"
- 被跳转到登录页

### 根本原因
1. Zustand 的 `persist` 中间件需要时间从 localStorage 恢复状态
2. PrivateRoute 组件在渲染时立即检查 `isAuthenticated`
3. 此时 hydration 还没完成，`isAuthenticated` 仍为初始值 `false`
4. 导致已登录用户被误判为未登录

### 技术方案
- 在 authStore 中添加 `_hasHydrated` 状态
- 使用 `onRehydrateStorage` 回调在恢复完成后设置 `_hasHydrated = true`
- PrivateRoute 等待 `_hasHydrated` 为 true 后再判断 `isAuthenticated`

## Work Objectives

### Core Objective
确保页面刷新后认证状态正确恢复，已登录用户不会被跳转到登录页。

### Concrete Deliverables
- `frontend/src/stores/authStore.ts` - 添加 hydration 状态跟踪
- `frontend/src/components/PrivateRoute/index.tsx` - 等待 hydration 完成

### Definition of Done
- [ ] 刷新 `/api-config` 页面不再跳转到登录页
- [ ] 刷新其他受保护页面正常工作
- [ ] 未登录用户仍被正确跳转到登录页

## TODOs

- [ ] 1. 修改 authStore.ts 添加 hydration 状态

  **What to do**:
  - 添加 `_hasHydrated: boolean` 状态字段
  - 添加 `setHasHydrated` 方法
  - 配置 `onRehydrateStorage` 回调
  - 使用 `createJSONStorage` 替代默认 storage

  **File**: `frontend/src/stores/authStore.ts`

  **New Code**:
  ```typescript
  import { create } from 'zustand'
  import { persist, createJSONStorage } from 'zustand/middleware'

  interface User {
    id: string
    username: string
    email: string
    avatar: string
  }

  interface AuthState {
    token: string | null
    user: User | null
    isAuthenticated: boolean
    _hasHydrated: boolean
    setToken: (token: string) => void
    setUser: (user: User) => void
    logout: () => void
    setHasHydrated: (state: boolean) => void
  }

  export const useAuthStore = create<AuthState>()(
    persist(
      (set) => ({
        token: null,
        user: null,
        isAuthenticated: false,
        _hasHydrated: false,
        setToken: (token) => set({ token, isAuthenticated: true }),
        setUser: (user) => set({ user }),
        logout: () => set({ token: null, user: null, isAuthenticated: false, _hasHydrated: true }),
        setHasHydrated: (state) => set({ _hasHydrated: state }),
      }),
      {
        name: 'auth-storage',
        storage: createJSONStorage(() => localStorage),
        onRehydrateStorage: () => (state) => {
          state?.setHasHydrated(true)
        },
      }
    )
  )
  ```

- [ ] 2. 修改 PrivateRoute 等待 hydration 完成

  **What to do**:
  - 导入 `_hasHydrated` 状态
  - hydration 未完成时显示 loading 状态
  - hydration 完成后再判断 `isAuthenticated`

  **File**: `frontend/src/components/PrivateRoute/index.tsx`

  **New Code**:
  ```typescript
  import React from 'react'
  import { Navigate } from 'react-router-dom'
  import { Spin } from 'antd'
  import { useAuthStore } from '../../stores/authStore'

  interface PrivateRouteProps {
    children: React.ReactNode
  }

  const PrivateRoute: React.FC<PrivateRouteProps> = ({ children }) => {
    const { isAuthenticated, _hasHydrated } = useAuthStore()
    
    // 等待 Zustand persist 恢复完成
    if (!_hasHydrated) {
      return (
        <div style={{ 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center', 
          height: '100vh' 
        }}>
          <Spin size="large" />
        </div>
      )
    }
    
    return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />
  }

  export default PrivateRoute
  ```

## Verification Strategy

### Test Scenarios
1. **刷新测试**: 登录后访问 `/api-config`，刷新页面，应停留在当前页面
2. **未登录测试**: 清除 localStorage，访问 `/api-config`，应跳转到登录页
3. **其他页面测试**: 登录后访问其他受保护页面刷新，应正常工作

### Verification Commands
```bash
# 前端构建验证
cd frontend && npm run build
```

## Success Criteria

- [ ] 刷新页面不再显示"您未获授权"
- [ ] 未登录用户仍被正确拦截
- [ ] 前端构建无错误

---

运行 `/start-work fix-auth-hydration` 开始执行此计划。