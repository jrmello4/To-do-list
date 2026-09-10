const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch({ headless: true });
  const p = await b.newPage({ viewport: { width: 1440, height: 900 }, colorScheme: 'dark' });
  const errs = [];
  const apis = [];
  p.on('console', (m) => { if (m.type() === 'error') errs.push(m.text().slice(0, 200)); });
  p.on('response', (r) => {
    if (r.url().includes('/api/esportes')) apis.push(r.status() + ' ' + r.url().split('/api')[1]);
  });
  await p.goto('http://localhost:8080', { waitUntil: 'domcontentloaded' });
  await p.evaluate(() => localStorage.clear());
  await p.reload({ waitUntil: 'domcontentloaded' });
  await p.waitForTimeout(800);
  await p.locator('#loginEmail').fill('sport@teste.com');
  await p.locator('#loginPassword').fill('SenhaForte123');
  await p.locator('#loginForm button').filter({ hasText: /Entrar/i }).first().click({ timeout: 5000 });
  await p.waitForTimeout(4500);
  const cards = await p.locator('#dashSportsList .sports-match-card').count();
  const empty = await p.locator('#dashSportsList .dash-empty').count();
  const text = (await p.locator('#dashSportsList').innerText()).slice(0, 280);
  console.log(JSON.stringify({ cards, empty, apis, errs: errs.slice(0, 6) }, null, 2));
  console.log('text', text.replace(/\n/g, ' | '));
  await p.screenshot({ path: 'C:/Users/adenilson.j/Projects/java/output/playwright/E1-login-normal.png' });
  await b.close();
})().catch((e) => { console.error(e); process.exit(1); });
