const fs = require('fs');
const path = require('path');

const pagesDir = 'E:/AIdeom/智壤卫士/code/html/src/views/pages';
const allFiles = fs.readdirSync(pagesDir).filter(f => f.endsWith('.vue'));

// 20 个调用了 API 的页面
const withApi = [
  'OrgManage.vue', 'RoleManage.vue', 'UserManage.vue', 'SoilErosion.vue',
  'RockStratumAnalysis.vue', 'AnnouncementBoard.vue', 'GeoStandard.vue',
  'Desertification.vue', 'ClimateWarming.vue', 'WorkflowDesign.vue',
  'EcoStandard.vue', 'DataExport.vue', 'Device.vue', 'Dashboard.vue',
  'MissionList.vue', 'DataImport.vue', 'OpenApiManage.vue',
  'ReportCenter.vue', 'DisasterRisk.vue', 'QualityCheck.vue'
];

const noApiFiles = allFiles.filter(f => !withApi.includes(f));

console.log('\n========== 未调用后端 API 的页面（共 ' + noApiFiles.length + ' 个） ==========\n');

for (const file of noApiFiles) {
  const content = fs.readFileSync(path.join(pagesDir, file), 'utf-8');
  
  // 检查是否有硬编码的数组数据
  const hardcodedMatches = [];
  const re = /const\s+(\w+)\s*=\s*ref\(\[\s*\{[\s\S]*?\}\s*\]\)/g;
  let m;
  while ((m = re.exec(content)) !== null) {
    const snippet = m[0].replace(/\s+/g, ' ').substring(0, 100);
    if (m[0].length > 80) {
      hardcodedMatches.push({ name: m[1], snippet });
    }
  }
  
  // 检查图表硬编码
  const hasChartHardcode = /xAxis:\s*\{[\s\S]*?data:\s*\[.*'/.test(content) || 
                          /series:\s*\[[\s\S]*?data:\s*\[/.test(content);
  
  // 检查表格/列表数据是否来自 onMounted 中的 API 调用
  const onMountedMatch = content.match(/onMounted\s*\(\s*(?:async\s+)?\(\s*\)\s*=>\s*\{[\s\S]*?\}\s*\)/);
  let onMountedSummary = '';
  if (onMountedMatch) {
    const body = onMountedMatch[0];
    const hasApiCall = /await\s+\w+\s*\(/.test(body) || /\.then\s*\(/.test(body);
    const hasFetch = /fetch\s*\(/.test(body);
    onMountedSummary = hasApiCall ? '有异步调用' : '纯初始化';
  }
  
  console.log(`📄 ${file}`);
  console.log(`   onMounted: ${onMountedSummary || '无'}`);
  console.log(`   硬编码数组: ${hardcodedMatches.length} 个`);
  hardcodedMatches.forEach(h => console.log(`   - ${h.name}: ${h.snippet}...`));
  console.log(`   图表硬编码: ${hasChartHardcode ? '是' : '否'}`);
  console.log('');
}
