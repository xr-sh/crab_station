import React, { useEffect } from 'react'
import {
  Modal,
  Form,
  Input,
  DatePicker,
  InputNumber,
  Select,
  message,
} from 'antd'
import dayjs from 'dayjs'
import { financeApi, FinanceRecord, CreateFinanceRecordRequest, UpdateFinanceRecordRequest } from '../../../api/finance'

interface FinanceModalProps {
  visible: boolean
  onCancel: () => void
  onSuccess: () => void
  record: FinanceRecord | null
}

const FinanceModal: React.FC<FinanceModalProps> = ({
  visible,
  onCancel,
  onSuccess,
  record,
}) => {
  const [form] = Form.useForm()
  const isEditing = !!record

  useEffect(() => {
    if (visible && record) {
      form.setFieldsValue({
        recordDate: dayjs(record.recordDate),
        amount: record.amount,
        type: record.type,
        remark: record.remark,
      })
    } else if (visible) {
      form.resetFields()
      form.setFieldsValue({
        recordDate: dayjs(),
      })
    }
  }, [visible, record, form])

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      
      const data = {
        ...values,
        recordDate: values.recordDate.format('YYYY-MM-DD'),
      }

      if (isEditing) {
        await financeApi.updateFinanceRecord(record.id, data as UpdateFinanceRecordRequest)
        message.success('更新成功')
      } else {
        await financeApi.createFinanceRecord(data as CreateFinanceRecordRequest)
        message.success('创建成功')
      }
      
      onSuccess()
    } catch (error: any) {
      message.error(error.message || (isEditing ? '更新失败' : '创建失败'))
    }
  }

  return (
    <Modal
      title={isEditing ? '编辑财务记录' : '新增财务记录'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      okText="保存"
      cancelText="取消"
      width={500}
    >
      <Form
        form={form}
        layout="vertical"
        autoComplete="off"
      >
        <Form.Item
          name="recordDate"
          label="日期"
          rules={[{ required: true, message: '请选择日期' }]}
        >
          <DatePicker style={{ width: '100%' }} />
        </Form.Item>

        <Form.Item
          name="type"
          label="类别"
          rules={[{ required: true, message: '请选择类别' }]}
        >
          <Select
            placeholder="请选择类别"
            options={[
              { value: '收入', label: '收入' },
              { value: '支出', label: '支出' },
            ]}
          />
        </Form.Item>

        <Form.Item
          name="amount"
          label="金额"
          rules={[{ required: true, message: '请输入金额' }]}
        >
          <InputNumber
            style={{ width: '100%' }}
            min={0.01}
            precision={2}
            placeholder="请输入金额"
            addonBefore="¥"
          />
        </Form.Item>

        <Form.Item
          name="remark"
          label="备注"
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

export default FinanceModal
