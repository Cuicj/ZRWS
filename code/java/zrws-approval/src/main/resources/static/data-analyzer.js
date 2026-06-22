// AI数据适配器 - 前端JavaScript
const API_BASE = '/api/v1/dataanalyzer';

// 当前面板
let currentPanel = 'dashboard';

// 切换面板
function showPanel(panelId) {
    document.querySelectorAll('.panel').forEach(p => p.style.display = 'none');
    document.querySelectorAll('.sidebar nav a').forEach(a => a.classList.remove('active'));
    
    document.getElementById(panelId).style.display = 'block';
    document.querySelector(`a[href="#${panelId}"]`).classList.add('active');
    
    currentPanel = panelId;
    
    // 更新标题
    const titles = {
        'dashboard': '数据概览',
        'import': '数据导入',
        'preview': '数据预览',
        'approval-config': '审批配置',
        'stats': '数据统计',
        'history': '导入历史'
    };
    document.getElementById('page-title').textContent = titles[panelId];
    
    // 加载数据
    if (panelId === 'dashboard') loadDashboard();
    if (panelId === 'approval-config') loadApprovalConfigs();
    if (panelId === 'stats') loadStatistics();
    if (panelId === 'history') loadHistory();
}

// 加载仪表盘数据
function loadDashboard() {
    fetch(`${API_BASE}/batches`)
        .then(res => res.json())
        .then(data => {
            if (!data.success) return;
            
            const today = new Date().toISOString().split('T')[0];
            const todayBatches = data.batches.filter(b => 
                b.createdTime && b.createdTime.startsWith(today)
            );
            
            const success = todayBatches.filter(b => b.status === 'SUCCESS').length;
            const failed = todayBatches.filter(b => b.status === 'FAILED').length;
            const partial = todayBatches.filter(b => b.status === 'PARTIAL').length;
            
            document.getElementById('stat-total').textContent = todayBatches.length;
            document.getElementById('stat-success').textContent = success;
            document.getElementById('stat-partial').textContent = partial;
            document.getElementById('stat-failed').textContent = failed;
        })
        .catch(err => console.error('加载仪表盘失败:', err));
}

// 预览文件
function previewFile() {
    const fileInput = document.getElementById('import-file');
    const boCode = document.getElementById('import-bo-code').value;
    
    if (!fileInput.files[0]) {
        showToast('请先选择文件', 'warning');
        return;
    }
    if (!boCode) {
        showToast('请选择业务对象', 'warning');
        return;
    }
    
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    formData.append('boCode', boCode);
    formData.append('useAiEnhance', document.getElementById('use-ai-mapping').checked);
    
    fetch(`${API_BASE}/upload-analyze`, {
        method: 'POST',
        body: formData
    })
    .then(res => res.json())
    .then(data => {
        if (!data.success) {
            showToast(data.error || '预览失败', 'danger');
            return;
        }
        
        // 保存分析结果供后续使用
        window.previewData = data;
        
        // 显示预览面板
        showPanel('preview');
        renderPreview(data);
    })
    .catch(err => {
        console.error('预览失败:', err);
        showToast('预览失败', 'danger');
    });
}

// 渲染预览
function renderPreview(data) {
    const content = document.getElementById('preview-content');
    
    let html = `
        <div style="margin-bottom: 20px;">
            <h4>数据概览</h4>
            <p>总行数: <strong>${data.totalRows}</strong> | 
               字段数: <strong>${data.fieldMappings?.length || 0}</strong></p>
        </div>
        
        <div style="margin-bottom: 20px;">
            <h4>字段映射</h4>
            <table>
                <tr><th>源字段</th><th>目标字段</th><th>置信度</th><th>说明</th></tr>
    `;
    
    data.fieldMappings?.forEach(m => {
        const confidence = (m.confidence * 100).toFixed(0) + '%';
        html += `<tr>
            <td>${m.sourceField}</td>
            <td>${m.targetField}</td>
            <td>${confidence}</td>
            <td>${m.description || '-'}</td>
        </tr>`;
    });
    
    html += `</table></div>`;
    
    // 数据预览
    html += `
        <div style="margin-bottom: 20px;">
            <h4>数据预览(前10行)</h4>
            <div class="data-preview">
                <table>
    `;
    
    if (data.dataPreview && data.dataPreview.length > 0) {
        const headers = Object.keys(data.dataPreview[0]);
        html += '<tr>' + headers.map(h => `<th>${h}</th>`).join('') + '</tr>';
        
        data.dataPreview.forEach(row => {
            html += '<tr>' + headers.map(h => `<td>${row[h] || ''}</td>`).join('') + '</tr>';
        });
    }
    
    html += `</table></div></div>`;
    
    // 校验结果
    if (data.validationSummary) {
        const vs = data.validationSummary;
        html += `
            <div style="margin-bottom: 20px;">
                <h4>校验结果</h4>
                <div style="display: flex; gap: 20px;">
                    <div style="flex: 1;">
                        <div class="badge ${vs.passedValidations === vs.totalValidations ? 'success' : 'warning'}">
                            通过: ${vs.passedValidations}/${vs.totalValidations}
                        </div>
                    </div>
                    <div style="flex: 1;">
                        <div class="badge ${vs.failedValidations > 0 ? 'danger' : 'success'}">
                            失败: ${vs.failedValidations}
                        </div>
                    </div>
                    <div style="flex: 1;">
                        <div class="badge ${vs.warningValidations > 0 ? 'warning' : 'success'}">
                            警告: ${vs.warningValidations}
                        </div>
                    </div>
                </div>
            </div>
        `;
    }
    
    // AI分析结果
    if (data.aiAnalysis) {
        const ai = data.aiAnalysis;
        html += `
            <div>
                <h4>AI分析结果</h4>
                <div style="margin-bottom: 10px;">
                    <span>数据质量评分: </span>
                    <span style="font-size: 24px; font-weight: bold; color: ${ai.qualityScore >= 80 ? '#27ae60' : ai.qualityScore >= 60 ? '#f39c12' : '#e74c3c'}">
                        ${ai.qualityScore}/100
                    </span>
                </div>
        `;
        
        if (ai.suggestions && ai.suggestions.length > 0) {
            html += '<div><h5>建议:</h5><ul>';
            ai.suggestions.forEach(s => html += `<li>${s}</li>`);
            html += '</ul></div>';
        }
        
        html += '</div>';
    }
    
    // 操作按钮
    html += `
        <div style="display: flex; gap: 10px; margin-top: 20px;">
            <button class="btn btn-success" onclick="confirmImport()">确认导入</button>
            <button class="btn btn-secondary" onclick="showPanel('import')">返回修改</button>
        </div>
    `;
    
    content.innerHTML = html;
}

// 确认导入
function confirmImport() {
    if (!window.previewData) {
        showToast('没有预览数据', 'warning');
        return;
    }
    
    document.getElementById('confirm-message').textContent = 
        `确定要导入 ${window.previewData.totalRows} 条数据吗?`;
    window.confirmCallback = doImport;
    document.getElementById('confirm-modal').classList.add('show');
}

// 执行导入
function doImport() {
    closeConfirmModal();
    
    const formData = new FormData();
    formData.append('file', document.getElementById('import-file').files[0]);
    formData.append('boCode', document.getElementById('import-bo-code').value);
    formData.append('importMode', document.getElementById('import-mode').value);
    formData.append('useAiMapping', document.getElementById('use-ai-mapping').checked);
    formData.append('autoFix', document.getElementById('auto-fix').checked);
    
    showToast('正在导入...', 'info');
    
    fetch(`${API_BASE}/upload-import`, {
        method: 'POST',
        body: formData
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            showToast(`导入成功! 成功: ${data.successRows}, 失败: ${data.failedRows}`, 'success');
            showPanel('dashboard');
            loadDashboard();
        } else {
            showToast(data.error || '导入失败', 'danger');
        }
    })
    .catch(err => {
        console.error('导入失败:', err);
        showToast('导入失败', 'danger');
    });
}

// 加载审批配置
function loadApprovalConfigs() {
    fetch(`${API_BASE}/approval-configs`)
        .then(res => res.json())
        .then(data => {
            if (!data.success) return;
            
            const tbody = document.querySelector('#approval-config-table tbody');
            tbody.innerHTML = '';
            
            data.configs.forEach(config => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${config.boName || config.boCode}</td>
                    <td>${getOperationDesc(config.operationType)}</td>
                    <td>${getApprovalLevelDesc(config.approvalLevel)}</td>
                    <td>${config.processName || '-'}</td>
                    <td>
                        <label class="switch">
                            <input type="checkbox" ${config.enableApproval === 1 ? 'checked' : ''} 
                                   onchange="toggleApproval(${config.configId}, this)">
                            <span class="slider"></span>
                        </label>
                    </td>
                    <td>${config.priority || 1}</td>
                    <td>
                        <button class="btn btn-secondary" style="padding: 4px 8px; font-size: 12px;" 
                                onclick="editApprovalConfig(${config.configId})">编辑</button>
                        <button class="btn btn-danger" style="padding: 4px 8px; font-size: 12px;" 
                                onclick="deleteApprovalConfig(${config.configId})">删除</button>
                    </td>
                `;
                tbody.appendChild(row);
            });
        })
        .catch(err => console.error('加载审批配置失败:', err));
}

// 获取操作类型描述
function getOperationDesc(type) {
    const map = { 'INSERT': '新增', 'UPDATE': '修改', 'DELETE': '删除', 'IMPORT': '导入' };
    return map[type] || type;
}

// 获取审批级别描述
function getApprovalLevelDesc(level) {
    const map = { 'SINGLE': '单级', 'MULTI': '多级', 'CUSTOM': '自定义' };
    return map[level] || level;
}

// 显示审批配置模态框
function showApprovalModal(configId) {
    document.getElementById('modal-title').textContent = configId ? '编辑审批配置' : '添加审批配置';
    document.getElementById('config-id').value = configId || '';
    
    if (configId) {
        fetch(`${API_BASE}/approval-config/${configId}`)
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    const config = data.config;
                    document.getElementById('config-bo-code').value = config.boCode;
                    document.getElementById('config-operation').value = config.operationType;
                    document.getElementById('config-level').value = config.approvalLevel;
                    document.getElementById('config-process-key').value = config.processKey || '';
                    document.getElementById('config-process-name').value = config.processName || '';
                    document.getElementById('config-priority').value = config.priority || 1;
                    document.getElementById('config-enable').checked = config.enableApproval === 1;
                    document.getElementById('config-condition').value = config.conditionExpr || '';
                    document.getElementById('config-description').value = config.description || '';
                }
            });
    } else {
        document.getElementById('approval-form').reset();
        document.getElementById('config-priority').value = 1;
    }
    
    document.getElementById('approval-modal').classList.add('show');
}

// 编辑审批配置
function editApprovalConfig(configId) {
    showApprovalModal(configId);
}

// 删除审批配置
function deleteApprovalConfig(configId) {
    document.getElementById('confirm-message').textContent = '确定要删除这个审批配置吗?';
    window.confirmCallback = () => {
        fetch(`${API_BASE}/approval-config/${configId}`, { method: 'DELETE' })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    showToast('删除成功', 'success');
                    loadApprovalConfigs();
                } else {
                    showToast('删除失败', 'danger');
                }
            });
    };
    document.getElementById('confirm-modal').classList.add('show');
}

// 保存审批配置
function saveApprovalConfig() {
    const config = {
        configId: document.getElementById('config-id').value || null,
        boCode: document.getElementById('config-bo-code').value,
        operationType: document.getElementById('config-operation').value,
        approvalLevel: document.getElementById('config-level').value,
        processKey: document.getElementById('config-process-key').value,
        processName: document.getElementById('config-process-name').value,
        priority: parseInt(document.getElementById('config-priority').value),
        enableApproval: document.getElementById('config-enable').checked ? 1 : 0,
        conditionExpr: document.getElementById('config-condition').value,
        description: document.getElementById('config-description').value
    };
    
    fetch(`${API_BASE}/approval-config`, {
        method: config.configId ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(config)
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            showToast('保存成功', 'success');
            closeApprovalModal();
            loadApprovalConfigs();
        } else {
            showToast(data.error || '保存失败', 'danger');
        }
    });
}

// 切换审批启用状态
function toggleApproval(configId, checkbox) {
    fetch(`${API_BASE}/approval-config/${configId}/toggle`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ enableApproval: checkbox.checked ? 1 : 0 })
    })
    .then(res => res.json())
    .then(data => {
        if (!data.success) {
            checkbox.checked = !checkbox.checked;
            showToast('操作失败', 'danger');
        }
    });
}

// 加载统计数据
function loadStatistics() {
    fetch(`${API_BASE}/stats/summary`)
        .then(res => res.json())
        .then(data => {
            if (!data.success) return;
            
            document.getElementById('stats-total-count').textContent = data.total || 0;
            document.getElementById('stats-success-rate').textContent = 
                (data.successRate || 0).toFixed(1) + '%';
            document.getElementById('stats-total-rows').textContent = data.totalRows || 0;
            document.getElementById('stats-quality').textContent = data.qualityScore || 0;
            
            // 加载详情
            const tbody = document.getElementById('stats-detail-body');
            tbody.innerHTML = '';
            
            if (data.byDate && data.byDate.length > 0) {
                data.byDate.forEach(d => {
                    const rate = d.total > 0 ? (d.success / d.total * 100).toFixed(0) : 0;
                    tbody.innerHTML += `
                        <tr>
                            <td>${d.date}</td>
                            <td>${d.total}</td>
                            <td>${d.success}</td>
                            <td>${d.total - d.success}</td>
                            <td>${rate}%</td>
                        </tr>
                    `;
                });
            }
            
            // 按BO统计
            const boBody = document.getElementById('stats-by-bo-body');
            boBody.innerHTML = '';
            
            if (data.byBo && data.byBo.length > 0) {
                data.byBo.forEach(bo => {
                    boBody.innerHTML += `
                        <tr>
                            <td>${bo.boName || bo.boCode}</td>
                            <td>${bo.totalCount || 0}</td>
                            <td>${bo.successCount || 0}</td>
                            <td>${bo.failedCount || 0}</td>
                            <td>${bo.totalRecords || 0}</td>
                            <td>${bo.approvedCount || 0}</td>
                            <td>${bo.qualityScore || '-'}</td>
                        </tr>
                    `;
                });
            }
        })
        .catch(err => console.error('加载统计失败:', err));
}

// 显示统计标签页
function showStatsTab(tab) {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    document.querySelector(`.tab:nth-child(${['overview', 'by-bo', 'trend'].indexOf(tab) + 1})`).classList.add('active');
    
    document.getElementById('stats-overview').style.display = tab === 'overview' ? 'block' : 'none';
    document.getElementById('stats-by-bo').style.display = tab === 'by-bo' ? 'block' : 'none';
    document.getElementById('stats-trend').style.display = tab === 'trend' ? 'block' : 'none';
}

// 加载历史记录
function loadHistory() {
    fetch(`${API_BASE}/batches`)
        .then(res => res.json())
        .then(data => {
            if (!data.success) return;
            
            const tbody = document.getElementById('history-body');
            tbody.innerHTML = '';
            
            data.batches.forEach(batch => {
                const statusClass = {
                    'SUCCESS': 'success',
                    'FAILED': 'danger',
                    'PARTIAL': 'warning',
                    'PROCESSING': 'info',
                    'PENDING': 'info'
                }[batch.status];
                
                tbody.innerHTML += `
                    <tr>
                        <td>${batch.batchNo}</td>
                        <td>${batch.boCode}</td>
                        <td>${batch.fileName || '-'}</td>
                        <td><span class="badge ${statusClass}">${getStatusDesc(batch.status)}</span></td>
                        <td>${batch.totalRows || 0}</td>
                        <td>${batch.successRows || 0}/${batch.failedRows || 0}</td>
                        <td>${batch.operatorName || '-'}</td>
                        <td>${formatTime(batch.createdTime)}</td>
                        <td>
                            <button class="btn btn-secondary" style="padding: 4px 8px; font-size: 12px;" 
                                    onclick="viewBatchDetail('${batch.batchNo}')">详情</button>
                        </td>
                    </tr>
                `;
            });
        })
        .catch(err => console.error('加载历史失败:', err));
}

// 获取状态描述
function getStatusDesc(status) {
    const map = {
        'PENDING': '待处理',
        'PROCESSING': '处理中',
        'SUCCESS': '成功',
        'FAILED': '失败',
        'PARTIAL': '部分成功'
    };
    return map[status] || status;
}

// 格式化时间
function formatTime(timeStr) {
    if (!timeStr) return '-';
    const date = new Date(timeStr);
    return date.toLocaleString('zh-CN');
}

// 查看批次详情
function viewBatchDetail(batchNo) {
    fetch(`${API_BASE}/batches/${batchNo}`)
        .then(res => res.json())
        .then(data => {
            if (!data.success) return;
            
            const batch = data.batch;
            let html = `
                <h4>批次详情</h4>
                <div style="margin-bottom: 15px;">
                    <p><strong>批次号:</strong> ${batch.batchNo}</p>
                    <p><strong>BO:</strong> ${batch.boCode}</p>
                    <p><strong>文件:</strong> ${batch.fileName}</p>
                    <p><strong>状态:</strong> <span class="badge ${statusClass}">${getStatusDesc(batch.status)}</span></p>
                    <p><strong>结果:</strong> 成功 ${batch.successRows || 0} / 失败 ${batch.failedRows || 0} / 跳过 ${batch.skippedRows || 0}</p>
                </div>
            `;
            
            showModal('批次详情', html);
        });
}

// 搜索历史
function searchHistory() {
    const keyword = document.getElementById('history-search').value;
    if (!keyword) {
        loadHistory();
        return;
    }
    
    fetch(`${API_BASE}/batches?keyword=${encodeURIComponent(keyword)}`)
        .then(res => res.json())
        .then(data => {
            if (!data.success) return;
            
            const tbody = document.getElementById('history-body');
            tbody.innerHTML = '';
            
            data.batches.forEach(batch => {
                const statusClass = {
                    'SUCCESS': 'success',
                    'FAILED': 'danger',
                    'PARTIAL': 'warning'
                }[batch.status];
                
                tbody.innerHTML += `
                    <tr>
                        <td>${batch.batchNo}</td>
                        <td>${batch.boCode}</td>
                        <td>${batch.fileName}</td>
                        <td><span class="badge ${statusClass}">${getStatusDesc(batch.status)}</span></td>
                        <td>${batch.totalRows || 0}</td>
                        <td>${batch.successRows || 0}/${batch.failedRows || 0}</td>
                        <td>${batch.operatorName || '-'}</td>
                        <td>${formatTime(batch.createdTime)}</td>
                        <td>
                            <button class="btn btn-secondary" style="padding: 4px 8px; font-size: 12px;" 
                                    onclick="viewBatchDetail('${batch.batchNo}')">详情</button>
                        </td>
                    </tr>
                `;
            });
        });
}

// 清空表单
function clearForm() {
    document.getElementById('import-form').reset();
    document.getElementById('use-ai-mapping').checked = true;
}

// 刷新数据
function refreshData() {
    showPanel(currentPanel);
}

// 关闭审批模态框
function closeApprovalModal() {
    document.getElementById('approval-modal').classList.remove('show');
}

// 关闭确认模态框
function closeConfirmModal() {
    document.getElementById('confirm-modal').classList.remove('show');
}

// 执行确认操作
function executeConfirm() {
    if (window.confirmCallback) {
        window.confirmCallback();
    }
    closeConfirmModal();
}

// 显示模态框
function showModal(title, content) {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay show';
    modal.innerHTML = `
        <div class="modal" style="max-width: 500px;">
            <div class="modal-header">
                <div class="modal-title">${title}</div>
                <div class="modal-close" onclick="this.closest('.modal-overlay').remove()">×</div>
            </div>
            <div class="modal-body">${content}</div>
            <div class="modal-footer">
                <button class="btn btn-secondary" onclick="this.closest('.modal-overlay').remove()">关闭</button>
            </div>
        </div>
    `;
    document.body.appendChild(modal);
}

// 显示提示消息
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    
    const colors = {
        'success': '#27ae60',
        'warning': '#f39c12',
        'danger': '#e74c3c',
        'info': '#3498db'
    };
    toast.style.background = colors[type] || colors.info;
    toast.style.display = 'block';
    
    setTimeout(() => {
        toast.style.display = 'none';
    }, 3000);
}

// 表单提交处理
document.getElementById('import-form').addEventListener('submit', (e) => {
    e.preventDefault();
    previewFile();
});

// 初始化
document.addEventListener('DOMContentLoaded', () => {
    loadDashboard();
});