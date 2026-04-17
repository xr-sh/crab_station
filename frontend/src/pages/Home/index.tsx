import React from 'react'
import { Card, Statistic, Row, Col, Typography } from 'antd'
import {
  UserOutlined,
  TeamOutlined,
  LoginOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons'
import { useAuthStore } from '../../stores/authStore'

const { Title, Text } = Typography

const Home: React.FC = () => {
  const { user } = useAuthStore()

  return (
    <div style={{ padding: 24 }}>
      <Title level={3}>欢迎回来，{user?.username || '用户'}！</Title>
      <Text type="secondary">这是工作站的首页，您可以在这里查看系统概况</Text>

      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col xs={24} md={12} xl={6}>
          <Card>
            <Statistic
              title="总用户数"
              value={0}
              prefix={<TeamOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card>
            <Statistic
              title="活跃用户"
              value={0}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card>
            <Statistic
              title="今日登录"
              value={0}
              prefix={<LoginOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card>
            <Statistic
              title="当前用户"
              value={user?.username || '-'}
              prefix={<UserOutlined />}
            />
          </Card>
        </Col>
      </Row>

      <Card style={{ marginTop: 24 }} title="功能说明">
        <ul>
          <li>用户管理：查看、添加、编辑、删除用户</li>
          <li>状态管理：启用或禁用用户账号</li>
          <li>搜索功能：按用户名或邮箱搜索用户</li>
          <li>密码重置：编辑用户时可重置密码</li>
        </ul>
      </Card>
    </div>
  )
}

export default Home
