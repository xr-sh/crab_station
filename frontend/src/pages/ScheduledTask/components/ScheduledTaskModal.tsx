import React, { useEffect } from 'react'
import { Modal, Form, Input, Select, Switch, message, Typography } from 'antd'
import { scheduledTaskApi, ScheduledTask as ScheduledTaskType, CreateScheduledTaskRequest, UpdateScheduledTaskRequest } from '../../../api/scheduledTask'

const { Text } = Typography

interface ScheduledTaskModalProps {
  visible: boolean
  onCancel: () => void
  onSuccess: () => void
  task: ScheduledTaskType | null
}

const taskTypeOptions = [
  { value: 'DATA_SYNC', label: '数据同步' },
  { value: 'DATA_CLEAN', label: '数据清理' },
  { value: 'REPORT_GEN', label: '报表生成' },
  { value: 'API_CALL', label: 'API调用' },
  { value: 'OTHER', label: '其他' },
]

const ScheduledTaskModal: React.FC<ScheduledTaskModalProps> = ({
  visible,
  onCancel,
  onSuccess,
  task,
}) => {
  const [form] = Form.useForm()
  const isEditing = !!task

  useEffect(() => {
    if (visible) {
      if (task) {
        form.setFieldsValue({
          taskName: task.taskName,
          taskType: task.taskType,
          cronExpression: task.cronExpression,
          taskParams: task.taskParams,
          status: task.status === 1,
          remark: task.remark,
        })
      } else {
        form.resetFields()
        form.setFieldsValue({ status: true })
      }
    }
  }, [visible, task, form])

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()

      const submitData = {
        ...values,
        status: values.status ? 1 : 0,
      }

      if (isEditing) {
        await scheduledTaskApi.updateScheduledTask(task!.id, submitData as UpdateScheduledTaskRequest)
        message.success('定时任务更新成功')
      } else {
        await scheduledTaskApi.createScheduledTask(submitData as CreateScheduledTaskRequest)
        message.success('定时任务创建成功')
      }

      onSuccess()
    } catch (error: any) {
      if (error.errorFields) {
        return
      }
      message.error(error.message || (isEditing ? '更新失败' : '创建失败'))
    }
  }

  return (
    <Modal
      title={isEditing ? '编辑定时任务' : '新增定时任务'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      okText={isEditing ? '更新' : '创建'}
      cancelText="取消"
      width={600}
    >
      <Form
        form={form}
        layout="vertical"
        autoComplete="off"
        style={{ marginTop: 16 }}
      >
        <Form.Item
          label="任务名称"
          name="taskName"
          rules={[
            { required: true, message: '请输入任务名称' },
            { max: 100, message: '任务名称最多100个字符' },
          ]}
        >
          <Input placeholder="如：每日数据同步、每周报表生成" />
        </Form.Item>

        <Form.Item
          label="任务类型"
          name="taskType"
          rules={[
            { required: true, message: '请选择任务类型' },
          ]}
        >
          <Select placeholder="请选择任务类型" options={taskTypeOptions} />
        </Form.Item>

        <Form.Item
          label="Cron表达式"
          name="cronExpression"
          rules={[
            { required: true, message: '请输入Cron表达式' },
            { max: 100, message: 'Cron表达式最多100个字符' },
          ]}
          extra={
            <Text type="secondary" style={{ fontSize: 12 }}>
              常用示例：每天凌晨2点执行 - 0 0 2 * * ? | 每小时执行 - 0 0 * * * ? | 每5分钟执行 - 0 0/5 * * * ?
            </Text>
          }
        >
          <Input placeholder="如：0 0 2 * * ?" />
        </Form.Item>

        <Form.Item
          label="任务参数（可选）"
          name="taskParams"
          rules={[
            { max: 2000, message: '任务参数最多2000个字符' },
          ]}
        >
          <Input.TextArea
            rows={4}
            placeholder='JSON格式的任务参数，如：{ "source": "api", "target": "database" }'
          />
        </Form.Item>

        <Form.Item
          label="状态"
          name="status"
          valuePropName="checked"
        >
          <Switch checkedChildren="启用" unCheckedChildren="禁用" defaultChecked />
        </Form.Item>

        <Form.Item
          label="备注"
          name="remark"
        >
          <Input.TextArea
            rows={3}
            placeholder="请输入备注（选填）"
            maxLength={500}
            showCount
          />
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default ScheduledTaskModal