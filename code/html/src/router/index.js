/**
 * 路由配置 - 智壤卫士 Vue 3 SPA
 * 采用懒加载，按需加载各模块页面
 */
import { createRouter, createWebHashHistory } from 'vue-router';

const routes = [
  // 根路径重定向到登录页
  {
    path: '/',
    redirect: '/login'
  },

  // 门户首页（模块陈列）
  {
    path: '/portal',
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
        path: 'data-export',
        name: 'DataExport',
        component: () => import('@/views/pages/DataExport.vue'),
        meta: { group: '处理', title: '数据导出', icon: '↥' }
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

      // 土地资源管理
      {
        path: 'land-map',
        name: 'LandMap',
        component: () => import('@/views/pages/LandMap.vue'),
        meta: { group: '土地资源', title: '土地地图', icon: '◍' }
      },
      {
        path: 'geo-standard',
        name: 'GeoStandard',
        component: () => import('@/views/pages/GeoStandard.vue'),
        meta: { group: '土地资源', title: '地质标准库', icon: '📋' }
      },
      {
        path: 'soil-classify',
        name: 'SoilClassify',
        component: () => import('@/views/pages/SoilClassify.vue'),
        meta: { group: '土地资源', title: '土质分类', icon: '✦' }
      },
      {
        path: 'rock-stratum',
        name: 'RockStratumAnalysis',
        component: () => import('@/views/pages/RockStratumAnalysis.vue'),
        meta: { group: '土地资源', title: '岩层分析', icon: '🪨' }
      },
      {
        path: 'disaster-risk',
        name: 'DisasterRisk',
        component: () => import('@/views/pages/DisasterRisk.vue'),
        meta: { group: '土地资源', title: '灾害风险', icon: '◬' }
      },
      {
        path: 'area-calc',
        name: 'AreaCalc',
        component: () => import('@/views/pages/AreaCalc.vue'),
        meta: { group: '土地资源', title: '面积计算', icon: '◭' }
      },

      // 土地资源公告栏
      {
        path: 'announcement-board',
        name: 'AnnouncementBoard',
        component: () => import('@/views/pages/AnnouncementBoard.vue'),
        meta: { group: '土地资源', title: '公告栏', icon: '📰' }
      },

      // 生态环境监测
      {
        path: 'climate-warming',
        name: 'ClimateWarming',
        component: () => import('@/views/pages/ClimateWarming.vue'),
        meta: { group: '生态环境', title: '气候变暖监测', icon: '🌡️' }
      },
      {
        path: 'desertification',
        name: 'Desertification',
        component: () => import('@/views/pages/Desertification.vue'),
        meta: { group: '生态环境', title: '沙漠化监测', icon: '🏜️' }
      },
      {
        path: 'soil-erosion',
        name: 'SoilErosion',
        component: () => import('@/views/pages/SoilErosion.vue'),
        meta: { group: '生态环境', title: '水土流失监测', icon: '🌊' }
      },
      {
        path: 'eco-standard',
        name: 'EcoStandard',
        component: () => import('@/views/pages/EcoStandard.vue'),
        meta: { group: '生态环境', title: '生态标准库', icon: '📋' }
      },

      // GIS 地理信息
      {
        path: 'cad-viewer',
        name: 'CadViewer',
        component: () => import('@/views/pages/CadViewer.vue'),
        meta: { group: 'GIS', title: '三维地球', icon: '🌍' }
      },
      {
        path: 'cad-compare',
        name: 'CadCompare',
        component: () => import('@/views/pages/CadCompare.vue'),
        meta: { group: 'GIS', title: '图纸对比', icon: '◫' }
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
      },

      // 用户管理
      {
        path: 'user-manage',
        name: 'UserManage',
        component: () => import('@/views/pages/UserManage.vue'),
        meta: { group: '系统', title: '用户管理', icon: '◉' }
      },

      // 角色管理
      {
        path: 'role-manage',
        name: 'RoleManage',
        component: () => import('@/views/pages/RoleManage.vue'),
        meta: { group: '系统', title: '角色管理', icon: '◆' }
      },

      // 组织管理
      {
        path: 'org-manage',
        name: 'OrgManage',
        component: () => import('@/views/pages/OrgManage.vue'),
        meta: { group: '系统', title: '组织管理', icon: '◎' }
      },

      // 系统配置
      {
        path: 'sys-config',
        name: 'SysConfig',
        component: () => import('@/views/pages/SysConfig.vue'),
        meta: { group: '系统', title: '系统配置', icon: '⚙' }
      },

      // 公告管理
      {
        path: 'announcement',
        name: 'Announcement',
        component: () => import('@/views/pages/Announcement.vue'),
        meta: { group: '系统', title: '公告管理', icon: '✉' }
      },

      // 报表中心
      {
        path: 'report-center',
        name: 'ReportCenter',
        component: () => import('@/views/pages/ReportCenter.vue'),
        meta: { group: '系统', title: '报表中心', icon: '📊' }
      },

      // 对外接口管理
      {
        path: 'open-api',
        name: 'OpenApiManage',
        component: () => import('@/views/pages/OpenApiManage.vue'),
        meta: { group: '系统', title: '对外接口', icon: '🔌' }
      },

      // API调试工具
      {
        path: 'api-playground',
        name: 'ApiPlayground',
        component: () => import('@/views/pages/ApiPlayground.vue'),
        meta: { group: '系统', title: 'API调试', icon: '⚡' }
      }
    ]
  },

  // 404 - 重定向到登录页
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

// 路由守卫：检查登录状态
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} · 智壤卫士` : '智壤卫士';
  
  // 如果不是登录页且不是门户页且没有token，跳转到登录页
  if (to.name !== 'Login' && to.name !== 'Portal' && !localStorage.getItem('token')) {
    next('/login');
  } else {
    next();
  }
});

export default router;
