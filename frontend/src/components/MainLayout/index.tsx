import React, { useState, useEffect } from 'react'
import {
  Layout,
  Menu,
  Avatar,
  Dropdown,
  Space,
  Typography,
  Button,
  message,
  Drawer,
  Grid,
} from 'antd'
import {
  UserOutlined,
  TeamOutlined,
  LogoutOutlined,
  HomeOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DollarOutlined,
  ShoppingCartOutlined,
  AppstoreOutlined,
  ProfileOutlined,
  ShopOutlined,
  LinkOutlined,
  CarOutlined,
  ScheduleOutlined,
  ApiOutlined,
} from '@ant-design/icons'
import { useNavigate, useLocation, Outlet } from 'react-router-dom'
import { useAuthStore } from '../../stores/authStore'
import { authApi } from '../../api/auth'

const { Header, Sider, Content } = Layout
const { Title, Text } = Typography
const { useBreakpoint } = Grid

const MainLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false)
  const [drawerVisible, setDrawerVisible] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useAuthStore()
  const screens = useBreakpoint()

  // 判断是否为移动端（小于 lg 断点 992px）
  const isMobile = !screens.lg

  // 移动端时自动收起侧边栏
  useEffect(() => {
    if (isMobile) {
      setCollapsed(true)
    }
  }, [isMobile])

  // 获取当前选中的菜单键
  const getSelectedKeys = () => {
    const path = location.pathname
    return [path]
  }

  // 获取展开的子菜单键
  const getOpenKeys = () => {
    const path = location.pathname
    if (['/purchase-spec', '/platform-spec', '/specification-mapping'].includes(path)) {
      return ['spec']
    }
    if (['/api-config'].includes(path)) {
      return ['task']
    }
    return []
  }

  const [openKeys, setOpenKeys] = useState<string[]>(getOpenKeys())

  const handleLogout = async () => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('Logout error:', error)
    }
    logout()
    message.success('已退出登录')
    navigate('/login')
  }

  const menuItems = [
    {
      key: '/',
      icon: <HomeOutlined />,
      label: '首页',
    },
    {
      key: '/users',
      icon: <TeamOutlined />,
      label: '用户管理',
    },
    {
      key: '/finance',
      icon: <DollarOutlined />,
      label: '财务管理',
    },
    {
      key: '/purchase',
      icon: <ShoppingCartOutlined />,
      label: '进货管理',
    },
    {
      key: 'spec',
      icon: <AppstoreOutlined />,
      label: '规格管理',
      children: [
        {
          key: '/purchase-spec',
          icon: <ProfileOutlined />,
          label: '进货规格',
        },
        {
          key: '/platform-spec',
          icon: <ShopOutlined />,
          label: '平台规格',
        },
        {
          key: '/specification-mapping',
          icon: <LinkOutlined />,
          label: '规格映射',
        },
      ],
    },
    {
      key: '/express',
      icon: <CarOutlined />,
      label: '快递分析',
    },
    {
      key: 'task',
      icon: <ScheduleOutlined />,
      label: '任务管理',
      children: [
        {
          key: '/api-config',
          icon: <ApiOutlined />,
          label: 'API配置',
        },
      ],
    },
  ]

  const userMenuItems = [
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: handleLogout,
    },
  ]

  // 菜单点击处理
  const handleMenuClick = (key: string) => {
    navigate(key)
    if (isMobile) {
      setDrawerVisible(false)
    }
  }

  // 侧边栏菜单内容（用于 Sider 和 Drawer）
  const siderContent = (
    <>
      <div
        style={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: collapsed && !isMobile ? 'center' : 'center',
          borderBottom: '1px solid #f0f0f0',
        }}
      >
        <Title
          level={collapsed && !isMobile ? 5 : 4}
          style={{
            margin: 0,
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
          }}
        >
          {collapsed && !isMobile ? '工作站' : '工作站'}
        </Title>
      </div>
      <Menu
        mode="inline"
        selectedKeys={getSelectedKeys()}
        openKeys={isMobile ? getOpenKeys() : openKeys}
        onOpenChange={(keys) => !isMobile && setOpenKeys(keys)}
        items={menuItems}
        onClick={({ key }) => handleMenuClick(key)}
        style={{ borderRight: 0 }}
      />
    </>
  )

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {/* 桌面端：固定侧边栏 */}
      {!isMobile && (
        <Sider
          trigger={null}
          collapsible
          collapsed={collapsed}
          theme="light"
          style={{
            boxShadow: '2px 0 8px rgba(0,0,0,0.05)',
            position: 'fixed',
            left: 0,
            top: 0,
            bottom: 0,
            zIndex: 100,
          }}
          width={220}
          collapsedWidth={80}
        >
          {siderContent}
        </Sider>
      )}

      {/* 移动端：抽屉式侧边栏 */}
      {isMobile && (
        <Drawer
          placement="left"
          onClose={() => setDrawerVisible(false)}
          open={drawerVisible}
          width={220}
          styles={{ body: { padding: 0 } }}
          title="工作站"
        >
          <Menu
            mode="inline"
            selectedKeys={getSelectedKeys()}
            openKeys={getOpenKeys()}
            onOpenChange={(keys) => setOpenKeys(keys)}
            items={menuItems}
            onClick={({ key }) => handleMenuClick(key)}
            style={{ borderRight: 0 }}
          />
        </Drawer>
      )}

      <Layout style={{ marginLeft: isMobile ? 0 : (collapsed ? 80 : 220) }}>
        <Header
          style={{
            background: '#fff',
            padding: isMobile ? '0 16px' : '0 24px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
            position: 'sticky',
            top: 0,
            zIndex: 99,
          }}
        >
          {/* 移动端显示菜单按钮，桌面端显示折叠按钮 */}
          {isMobile ? (
            <Button
              type="text"
              icon={<MenuUnfoldOutlined />}
              onClick={() => setDrawerVisible(true)}
              style={{ fontSize: 16 }}
            />
          ) : (
            <Button
              type="text"
              icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              onClick={() => setCollapsed(!collapsed)}
              style={{ fontSize: 16 }}
            />
          )}
          <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
            <Space style={{ cursor: 'pointer' }}>
              <Avatar icon={<UserOutlined />} src={user?.avatar} />
              {!isMobile && <Text>{user?.username || '用户'}</Text>}
            </Space>
          </Dropdown>
        </Header>
        <Content
          style={{
            margin: isMobile ? '16px 8px' : 24,
            background: '#fff',
            borderRadius: 8,
            minHeight: 280,
            overflow: 'auto',
          }}
        >
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}

export default MainLayout