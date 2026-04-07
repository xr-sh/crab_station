import React from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import Login from './pages/Login'
import Home from './pages/Home'
import Users from './pages/Users'
import Finance from './pages/Finance'
import Purchase from './pages/Purchase'
import PurchaseSpec from './pages/PurchaseSpec'
import PlatformSpec from './pages/PlatformSpec'
import SpecificationMapping from './pages/SpecificationMapping'
import Express from './pages/Express'
import PrivateRoute from './components/PrivateRoute'
import MainLayout from './components/MainLayout'

const App: React.FC = () => {
  return (
    <ConfigProvider locale={zhCN}>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/"
            element={
              <PrivateRoute>
                <MainLayout />
              </PrivateRoute>
            }
          >
            <Route index element={<Home />} />
            <Route path="users" element={<Users />} />
            <Route path="finance" element={<Finance />} />
            <Route path="purchase" element={<Purchase />} />
            <Route path="express" element={<Express />} />
            <Route path="purchase-spec" element={<PurchaseSpec />} />
            <Route path="platform-spec" element={<PlatformSpec />} />
            <Route path="specification-mapping" element={<SpecificationMapping />} />
          </Route>
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </Router>
    </ConfigProvider>
  )
}

export default App
