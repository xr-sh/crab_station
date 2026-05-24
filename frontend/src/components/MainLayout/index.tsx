import React, { useEffect, useState } from 'react'
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
  ShoppingCartOutlined,
  AppstoreOutlined,
  ProfileOutlined,
  ShopOutlined,
  LinkOutlined,
  CarOutlined,
  ScheduleOutlined,
  ApiOutlined,
  ClockCircleOutlined,
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
  const isMobile = !screens.lg

  useEffect(() => {
    if (isMobile) {
      setCollapsed(true)
    }
  }, [isMobile])

  const getSelectedKeys = () => {
    return [location.pathname]
  }

  const getOpenKeys = () => {
    const path = location.pathname
    if (['/purchase-spec', '/platform-spec', '/platform-package', '/specification-mapping'].includes(path)) {
      return ['spec']
    }
    if (['/api-config', '/scheduled-task'].includes(path)) {
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
      icon: <span style={{ fontWeight: 600 }}>¥</span>,
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
          key: '/platform-package',
          icon: <AppstoreOutlined />,
          label: '平台套餐',
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
        {
          key: '/scheduled-task',
          icon: <ClockCircleOutlined />,
          label: '定时任务',
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

  const handleMenuClick = (key: string) => {
    if (key.startsWith('/')) {
      navigate(key)
      if (isMobile) {
        setDrawerVisible(false)
      }
    }
  }

  const siderContent = (
    <>
      <div
        style={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
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
          工作站
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
