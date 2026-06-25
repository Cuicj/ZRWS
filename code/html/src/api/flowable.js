import request from '@/utils/request';

export function getProcessDefinitionList(params) {
  return request({
    url: '/v1/flowable/definitions',
    method: 'get',
    params
  });
}

export function getProcessDefinitionXML(id) {
  return request({
    url: `/v1/flowable/definitions/${id}/xml`,
    method: 'get'
  });
}

export function deployBPMN(data) {
  return request({
    url: '/v1/designer/save',
    method: 'post',
    data
  });
}

export function validateBPMN(data) {
  return request({
    url: '/v1/designer/validate',
    method: 'post',
    data
  });
}

export function getTemplateList() {
  return request({
    url: '/v1/designer/templates',
    method: 'get'
  });
}

export function getDraftList() {
  return request({
    url: '/v1/designer/drafts',
    method: 'get'
  });
}

export function getDraft(processKey) {
  return request({
    url: `/v1/designer/drafts/${processKey}`,
    method: 'get'
  });
}

export function saveDraft(data) {
  return request({
    url: '/v1/designer/drafts',
    method: 'post',
    data
  });
}

export function submitForReview(processKey) {
  return request({
    url: `/v1/designer/drafts/${processKey}/submit`,
    method: 'post'
  });
}

export function reviewProcess(processKey, data) {
  return request({
    url: `/v1/designer/drafts/${processKey}/review`,
    method: 'post',
    data
  });
}

export function publishProcess(processKey) {
  return request({
    url: `/v1/designer/drafts/${processKey}/publish`,
    method: 'post'
  });
}

export function deployPublished(processKey) {
  return request({
    url: `/v1/designer/drafts/${processKey}/deploy`,
    method: 'post'
  });
}

export function createFromTemplate(templateKey) {
  return request({
    url: `/v1/designer/template/${templateKey}/create`,
    method: 'post'
  });
}

export function xmlToJson(xml) {
  return request({
    url: '/v1/designer/xml-to-json',
    method: 'post',
    data: xml,
    headers: {
      'Content-Type': 'application/xml'
    }
  });
}

export function jsonToXml(json) {
  return request({
    url: '/v1/designer/json-to-xml',
    method: 'post',
    data: json
  });
}
