import React, { useEffect } from 'react'
import { Modal, Form, Input, Switch, message } from 'antd'
import { userApi, CreateUserRequest, UpdateUserRequest, User } from '../../../api/users'

interface UserModalProps {
  visible: boolean
  onCancel: () => void
  onSuccess: () => void
  user: User | null
}

const UserModal: React.FC<UserModalProps> = ({ visible, onCancel, onSuccess, user }) => {
  const [form] = Form.useForm()
  const isEditing = !!user

  useEffect(() => {
    if (visible) {
      if (user) {
        form.setFieldsValue({
          username: user.username,
          email: user.email,
          phone: user.phone,
          avatar: user.avatar,
          status: user.status === 1,
        })
      } else {
        form.resetFields()
        form.setFieldsValue({ status: true })
      }
    }
  }, [visible, user, form])

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      
      const submitData = {
        ...values,
        status: values.status ? 1 : 0,
      }

      if (isEditing) {
        const updateData: UpdateUserRequest = {
          email: submitData.email,
          phone: submitData.phone,
          avatar: submitData.avatar,
          status: submitData.status,
        }
        // 如果密码不为空，则包含密码
        if (submitData.password) {
          updateData.password = submitData.password
        }
        await userApi.updateUser(user!.id, updateData)
        message.success('用户更新成功')
      } else {
        const createData: CreateUserRequest = {
          username: submitData.username,
          password: submitData.password,
          email: submitData.email,
          phone: submitData.phone,
          avatar: submitData.avatar,
          status: submitData.status,
        }
        await userApi.createUser(createData)
        message.success('用户创建成功')
      }
      
      onSuccess()
    } catch (error: any) {
      if (error.errorFields) {
        // 表单验证错误
        return
      }
      message.error(error.message || (isEditing ? '更新失败' : '创建失败'))
    }
  }

  return (
    <Modal
      title={isEditing ? '编辑用户' : '添加用户'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      okText={isEditing ? '更新' : '创建'}
      cancelText="取消"
      width={500}
    >
      <Form
        form={form}
        layout="vertical"
        autoComplete="off"
        style={{ marginTop: 16 }}
      >
        <Form.Item
          label="用户名"
          name="username"
          rules={[
            { required: !isEditing, message: '请输入用户名' },
            { min: 3, message: '用户名至少3个字符' },
            { max: 50, message: '用户名最多50个字符' },
          ]}
        >
          <Input disabled={isEditing} placeholder="请输入用户名" />
        </Form.Item>

        <Form.Item
          label={isEditing ? '密码（留空表示不修改）' : '密码'}
          name="password"
          rules={[
            { required: !isEditing, message: '请输入密码' },
            { min: 6, message: '密码至少6个字符' },
            { max: 100, message: '密码最多100个字符' },
          ]}
        >
          <Input.Password placeholder={isEditing ? '留空表示不修改密码' : '请输入密码'} />
        </Form.Item>

        <Form.Item
          label="邮箱"
          name="email"
          rules={[
            { type: 'email', message: '请输入正确的邮箱格式' },
          ]}
        >
          <Input placeholder="请输入邮箱（选填）" />
        </Form.Item>

        <Form.Item
          label="手机号"
          name="phone"
        >
          <Input placeholder="请输入手机号（选填）" />
        </Form.Item>

        <Form.Item
          label="头像URL"
          name="avatar"
        >
          <Input placeholder="请输入头像URL（选填）" />
        </Form.Item>

        <Form.Item
          label="状态"
          name="status"
          valuePropName="checked"
        >
          <Switch checkedChildren="启用" unCheckedChildren="禁用" defaultChecked />
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default UserModal
