/**
 * 路由配置 - 智壤卫士 Vue 3 SPA
 * 采用懒加载，按需加载各模块页面
 */
import { createRouter, createWebHashHistory } from 'vue-router';

const routes = [
  // 门户首页（模块陈列）
  {
    path: '/',
    name: 'Portal',
    component: () => import('@/views/Portal.vue'),
    meta: { layout: 'blank', title: '智壤卫士' }
  },

  // 登录页
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { layout: 'blank', title: '登录' }
  },

  // 应用框架壳（带侧边栏）
  {
    path: '/app',
    name: 'AppShell',
    component: () => import('@/layouts/AppShell.vue'),
    redirect: '/app/dashboard',
    children: [
      // 运行仪表盘
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/pages/Dashboard.vue'),
        meta: { group: '总览', title: '运行仪表盘', icon: '◧' }
      },

      // 采集任务管理
      {
        path: 'mission-list',
        name: 'MissionList',
        component: () => import('@/views/pages/MissionList.vue'),
        meta: { group: '采集', title: '任务列表', icon: '◫' }
      },
      {
        path: 'flight-control',
        name: 'FlightControl',
        component: () => import('@/views/pages/FlightControl.vue'),
        meta: { group: '采集', title: '飞行控制', icon: '➤' }
      },
      {
        path: 'gps-track',
        name: 'GpsTrack',
        component: () => import('@/views/pages/GpsTrack.vue'),
        meta: { group: '采集', title: 'GPS 航迹', icon: '◎' }
      },
      {
        path: 'soil-sample',
        name: 'SoilSample',
        component: () => import('@/views/pages/SoilSample.vue'),
        meta: { group: '采集', title: '土壤采样', icon: '◉' }
      },

      // 数据处理与质检
      {
        path: 'data-import',
        name: 'DataImport',
        component: () => import('@/views/pages/DataImport.vue'),
        meta: { group: '处理', title: '数据导入', icon: '↧' }
      },
      {
        path: 'quality-check',
        name: 'QualityCheck',
        component: () => import('@/views/pages/QualityCheck.vue'),
        meta: { group: '处理', title: '质量校验', icon: '✓' }
      },
      {
        path: 'reconstruction',
        name: 'Reconstruction',
        component: () => import('@/views/pages/Reconstruction.vue'),
        meta: { group: '处理', title: '3D 重建', icon: '◰' }
      },

      // AI 智能分析
      {
        path: 'soil-classify',
        name: 'SoilClassify',
        component: () => import('@/views/pages/SoilClassify.vue'),
        meta: { group: 'AI', title: '土质分类', icon: '✦' }
      },
      {
        path: 'disaster-risk',
        name: 'DisasterRisk',
        component: () => import('@/views/pages/DisasterRisk.vue'),
        meta: { group: 'AI', title: '灾害风险', icon: '◬' }
      },
      {
        path: 'area-calc',
        name: 'AreaCalc',
        component: () => import('@/views/pages/AreaCalc.vue'),
        meta: { group: 'AI', title: '面积计算', icon: '◭' }
      },

      // CAD 图纸管理
      {
        path: 'cad-viewer',
        name: 'CadViewer',
        component: () => import('@/views/pages/CadViewer.vue'),
        meta: { group: 'CAD', title: '图纸查看器', icon: '▣' }
      },
      {
        path: 'cad-compare',
        name: 'CadCompare',
        component: () => import('@/views/pages/CadCompare.vue'),
        meta: { group: 'CAD', title: '图纸对比', icon: '◫' }
      },

      // 审批与工作流
      {
        path: 'approval-list',
        name: 'ApprovalList',
        component: () => import('@/views/pages/ApprovalList.vue'),
        meta: { group: '审批', title: '审批列表', icon: '◐' }
      },
      {
        path: 'workflow-design',
        name: 'WorkflowDesign',
        component: () => import('@/views/pages/WorkflowDesign.vue'),
        meta: { group: '审批', title: '流程设计', icon: '◭' }
      },

      // 设备管理
      {
        path: 'device',
        name: 'Device',
        component: () => import('@/views/pages/Device.vue'),
        meta: { group: '系统', title: '设备管理', icon: '⊞' }
      }
    ]
  },

  // 404
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

// 路由守卫：更新标题
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} · 智壤卫士` : '智壤卫士';
  next();
});

export default router;