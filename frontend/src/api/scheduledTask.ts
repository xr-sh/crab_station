import request from './request'

export interface ScheduledTask {
  id: string
  taskName: string
  taskType: string
  cronExpression: string
  taskParams?: string
  lastExecuteTime?: string
  nextExecuteTime?: string
  executeCount: number
  lastExecuteStatus?: string
  lastExecuteMessage?: string
  status: number
  remark?: string
  createdAt: string
  updatedAt: string
}

export interface CreateScheduledTaskRequest {
  taskName: string
  taskType: string
  cronExpression: string
  taskParams?: string
  status?: number
  remark?: string
}

export interface UpdateScheduledTaskRequest {
  taskName?: string
  taskType?: string
  cronExpression?: string
  taskParams?: string
  status?: number
  remark?: string
}

export interface GetScheduledTasksParams {
  page?: number
  size?: number
  sortBy?: string
  sortDirection?: string
  taskName?: string
  taskType?: string
  status?: number
}

export const scheduledTaskApi = {
  // 获取定时任务列表（分页）
  getScheduledTasks: (params: GetScheduledTasksParams) =>
    request.get('/scheduled-tasks', { params }),

  // 获取所有定时任务（不分页）
  getAllScheduledTasks: () =>
    request.get('/scheduled-tasks/all'),

  // 获取单个定时任务
  getScheduledTask: (id: string) =>
    request.get(`/scheduled-tasks/${id}`),

  // 创建定时任务
  createScheduledTask: (data: CreateScheduledTaskRequest) =>
    request.post('/scheduled-tasks', data),

  // 更新定时任务
  updateScheduledTask: (id: string, data: UpdateScheduledTaskRequest) =>
    request.put(`/scheduled-tasks/${id}`, data),

  // 删除定时任务
  deleteScheduledTask: (id: string) =>
    request.delete(`/scheduled-tasks/${id}`),

  // 切换任务状态
  toggleStatus: (id: string) =>
    request.patch(`/scheduled-tasks/${id}/toggle`),
}