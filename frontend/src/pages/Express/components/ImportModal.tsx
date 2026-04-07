import React, { useState } from 'react'
import {
  Modal,
  Upload,
  Button,
  message,
  List,
  Progress,
  Alert,
  Space,
  Typography,
  Select,
  Form,
} from 'antd'
import {
  UploadOutlined,
  FileExcelOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons'
import type { UploadFile } from 'antd/es/upload/interface'
import { expressApi, ImportResult, EXPRESS_CATEGORIES } from '../../../api/express'

const { Text } = Typography

interface ImportModalProps {
  visible: boolean
  onCancel: () => void
  onSuccess: () => void
}

const ImportModal: React.FC<ImportModalProps> = ({ visible, onCancel, onSuccess }) => {
  const [fileList, setFileList] = useState<UploadFile[]>([])
  const [category, setCategory] = useState<string | undefined>()
  const [loading, setLoading] = useState(false)
  const [results, setResults] = useState<ImportResult[]>([])
  const [importing, setImporting] = useState(false)

  const handleUpload = () => {
    if (fileList.length === 0) {
      message.warning('请选择要导入的文件')
      return
    }
    if (!category) {
      message.warning('请选择快递类别')
      return
    }

    const files = fileList.map(file => file.originFileObj as File)
    
    setLoading(true)
    setImporting(true)
    setResults([])

    expressApi.importExcel(files, category)
      .then((res: any) => {
        setResults(res as ImportResult[])
        
        // 检查是否全部成功
        const allSuccess = (res as ImportResult[]).every(r => r.success)
        if (allSuccess) {
          message.success('导入完成')
        } else {
          message.warning('部分文件导入失败，请查看详情')
        }
      })
      .catch((error: any) => {
        message.error(error.message || '导入失败')
      })
      .finally(() => {
        setLoading(false)
        setImporting(false)
      })
  }

  const handleClose = () => {
    setFileList([])
    setCategory(undefined)
    setResults([])
    setImporting(false)
    onCancel()
    if (results.some(r => r.success)) {
      onSuccess()
    }
  }

  const handleConfirm = () => {
    setFileList([])
    setCategory(undefined)
    setResults([])
    setImporting(false)
    onSuccess()
    onCancel()
  }

  return (
    <Modal
      title="导入快递数据"
      open={visible}
      onCancel={handleClose}
      width={700}
      footer={
        importing ? null : results.length > 0 ? (
          <Space>
            <Button onClick={handleClose}>关闭</Button>
            <Button type="primary" onClick={handleConfirm}>
              确定
            </Button>
          </Space>
        ) : (
          <Space>
            <Button onClick={onCancel}>取消</Button>
            <Button type="primary" loading={loading} onClick={handleUpload}>
              开始导入
            </Button>
          </Space>
        )
      }
    >
      {!importing && results.length === 0 && (
        <>
          <Alert
            message="导入说明"
            description={
              <ul style={{ marginBottom: 0, paddingLeft: 20 }}>
                <li>支持 .xlsx、.xls 格式的 Excel 文件</li>
                <li>可以同时选择多个文件上传</li>
                <li>第一行将作为表头，系统会自动识别列名</li>
                <li>每个文件的所有Sheet都会被导入</li>
              </ul>
            }
            type="info"
            showIcon
            style={{ marginBottom: 16 }}
          />

          <Form layout="vertical">
            <Form.Item label="快递类别" required>
              <Select
                placeholder="请选择快递类别"
                value={category}
                onChange={setCategory}
                options={EXPRESS_CATEGORIES.map(name => ({ value: name, label: name }))}
                style={{ width: '100%' }}
              />
            </Form.Item>
          </Form>

          <Upload
            multiple
            accept=".xlsx,.xls"
            fileList={fileList}
            beforeUpload={(file) => {
              setFileList([...fileList, file as unknown as UploadFile])
              return false
            }}
            onRemove={(file) => {
              const index = fileList.indexOf(file)
              const newFileList = fileList.slice()
              newFileList.splice(index, 1)
              setFileList(newFileList)
            }}
          >
            <Button icon={<UploadOutlined />}>选择Excel文件</Button>
          </Upload>

          {fileList.length > 0 && (
            <div style={{ marginTop: 16 }}>
              <Text type="secondary">已选择 {fileList.length} 个文件</Text>
            </div>
          )}
        </>
      )}

      {importing && (
        <div style={{ textAlign: 'center', padding: '40px 0' }}>
          <Progress type="circle" percent={100} status="active" />
          <div style={{ marginTop: 16 }}>
            <Text>正在导入数据，请稍候...</Text>
          </div>
        </div>
      )}

      {results.length > 0 && (
        <List
          header={<Text strong>导入结果</Text>}
          dataSource={results}
          renderItem={(item) => (
            <List.Item>
              <List.Item.Meta
                avatar={
                  item.success ? (
                    <CheckCircleOutlined style={{ color: '#52c41a', fontSize: 20 }} />
                  ) : (
                    <CloseCircleOutlined style={{ color: '#ff4d4f', fontSize: 20 }} />
                  )
                }
                title={
                  <Space>
                    <FileExcelOutlined />
                    <Text>{item.fileName}</Text>
                  </Space>
                }
                description={
                  item.success ? (
                    <div>
                      <Text type="success">{item.message}</Text>
                      {item.columns.length > 0 && (
                        <div style={{ marginTop: 8 }}>
                          <Text type="secondary">识别到的列: </Text>
                          <Text code>{item.columns.join(', ')}</Text>
                        </div>
                      )}
                      <div style={{ marginTop: 4 }}>
                        <Text type="secondary">
                          成功 {item.successRows} 行，失败 {item.failedRows} 行
                        </Text>
                      </div>
                    </div>
                  ) : (
                    <Text type="danger">{item.message}</Text>
                  )
                }
              />
            </List.Item>
          )}
        />
      )}
    </Modal>
  )
}

export default ImportModal