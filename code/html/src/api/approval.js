import request from '@/utils/request';

// 获取待办审批列表
export function getTodoList(params) {
  return request({
    url: '/v1/todo',
    method: 'get',
    params
  });
}

// 获取我申请的审批列表
export function getMyAppliedList(params) {
  return request({
    url: '/v1/my-applied',
    method: 'get',
    params
  });
}

// 获取审批详情
export function getApprovalDetail(id) {
  return request({
    url: `/v1/approval/${id}`,
    method: 'get'
  });
}

// 通过审批
export function approveApproval(id, data) {
  return request({
    url: `/v1/approval/${id}/approve`,
    method: 'post',
    data
  });
}

// 驳回审批
export function rejectApproval(id, data) {
  return request({
    url: `/v1/approval/${id}/reject`,
    method: 'post',
    data
  });
}

// 提交审批申请
export function submitApproval(data) {
  return request({
    url: '/v1/approval',
    method: 'post',
    data
  });
}