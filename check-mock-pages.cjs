const fs = require('fs');
const path = require('path');

const pagesDir = 'E:/AIdeom/智壤卫士/code/html/src/views/pages';
const files = fs.readdirSync(pagesDir).filter(f => f.endsWith('.vue'));

const results = [];

for (const file of files) {
  const content = fs.readFileSync(path.join(pagesDir, file), 'utf-8');
  const hasApiImport = /import.*from.*['"]@\/api['"]|import.*from.*['"]\.\.\/.*api['"]/.test(content);
  const hasRequestImport = /import.*request.*from/.test(content);
  
  // 查找 ref/reactive 中的硬编码数组/对象
  const hardcodedArrays = [];
  const refMatches = content.match(/const\s+\w+\s*=\s*ref\(\[[\s\S]*?\]\)/g) || [];
  const reactiveMatches = content.match(/const\s+\w+\s*=\s*reactive\(\[[\s\S]*?\]\)/g) || [];
  
  for (const m of [...refMatches, ...reactiveMatches]) {
    if (m.length > 50 && m.includes('name') && m.includes(':')) {
      hardcodedArrays.push(m.substring(0, 80).replace(/\s+/g, ' '));
    }
  }
  
  if (!hasApiImport && !hasRequestImport) {
    results.push({
      file,
      hasApiImport: false,
      hardcodedCount: hardcodedArrays.length,
      samples: hardcodedArrays.slice(0, 2)
    });
  }
}

console.log('\n========== 未使用 API 的页面（可能用了硬编码/mock 数据） ==========\n');
for (const r of results) {
  console.log(`📄 ${r.file}`);
  console.log(`   硬编码数组: ${r.hardcodedCount} 个`);
  if (r.samples.length > 0) {
    r.samples.forEach(s => console.log(`   - ${s}...`));
  }
  console.log('');
}

console.log(`共 ${results.length} 个页面未引入 API`);
