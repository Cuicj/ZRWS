const fs = require('fs');
const path = require('path');

const pagesDir = 'E:/AIdeom/智壤卫士/code/html/src/views/pages';
const files = fs.readdirSync(pagesDir).filter(f => f.endsWith('.vue'));

console.log('\n========== 扫描 API 数据适配问题 ==========\n');

for (const file of files) {
  const content = fs.readFileSync(path.join(pagesDir, file), 'utf-8');
  
  // 查找 API 调用和数据赋值
  const apiImports = [];
  const importMatches = content.match(/import\s*\{([^}]+)\}\s*from\s*['"]@\/api\//g) || [];
  importMatches.forEach(m => apiImports.push(m.replace(/\s+/g, ' ')));
  
  // 查找 res.data 和 res.list 的使用
  const dataPatterns = [];
  const dataRegex = /(\w+Res)\.data/g;
  let m;
  while ((m = dataRegex.exec(content)) !== null) {
    dataPatterns.push(m[0]);
  }
  
  const listPatterns = [];
  const listRegex = /(\w+Res)\.list/g;
  while ((m = listRegex.exec(content)) !== null) {
    listPatterns.push(m[0]);
  }
  
  // 查找 API 调用的 catch 回退值
  const catchPatterns = [];
  const catchRegex = /\.catch\(\(\)\s*=>\s*\(\{([^}]+)\}\)\)/g;
  while ((m = catchRegex.exec(content)) !== null) {
    catchPatterns.push(m[1].trim());
  }
  
  if (apiImports.length > 0) {
    console.log(`📄 ${file}`);
    console.log(`   API 导入: ${apiImports.length} 个`);
    if (dataPatterns.length > 0) console.log(`   .data 使用: ${[...new Set(dataPatterns)].join(', ')}`);
    if (listPatterns.length > 0) console.log(`   .list 使用: ${[...new Set(listPatterns)].join(', ')}`);
    if (catchPatterns.length > 0) console.log(`   catch 回退: ${catchPatterns.join(' | ')}`);
    console.log('');
  }
}
