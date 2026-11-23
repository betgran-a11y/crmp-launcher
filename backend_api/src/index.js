require('dotenv').config();
const express = require('express');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { Pool } = require('pg');

const app = express();
app.use(cors());
app.use(express.json());

const pool = new Pool({
  connectionString: process.env.DATABASE_URL
});

const JWT_SECRET = process.env.JWT_SECRET || 'dev_secret_key';
const RP_NICK_REGEX = /^[A-Z][a-z]{1,10}_[A-Z][a-z]{1,11}$/;

function authMiddleware(req, res, next) {
  const header = req.headers.authorization || '';
  const token = header.startsWith('Bearer ') ? header.slice(7) : null;
  if (!token) return res.status(401).json({ error: 'Missing token' });

  try {
    req.user = jwt.verify(token, JWT_SECRET);
    next();
  } catch {
    return res.status(401).json({ error: 'Invalid token' });
  }
}

app.get('/api/version', (req, res) => {
  res.json({
    latestVersion: process.env.CLIENT_LATEST_VERSION || '1.0.0',
    patchUrl: process.env.CLIENT_PATCH_URL || '',
    mandatory: false
  });
});

app.get('/api/servers', async (req, res) => {
  try {
    const { rows } = await pool.query(
      'SELECT id, name, host, port, online_count, max_players, status FROM game_servers ORDER BY id ASC'
    );
    res.json({ servers: rows });
  } catch (err) {
    res.status(500).json({ error: 'Database error' });
  }
});

app.post('/api/auth/register', async (req, res) => {
  const { nickname, email, password, referral } = req.body || {};
  if (!nickname || !RP_NICK_REGEX.test(nickname)) {
    return res.status(400).json({ error: 'Ник должен быть формата Name_Surname' });
  }
  if (!email || !email.includes('@')) {
    return res.status(400).json({ error: 'Некорректный Email' });
  }
  if (!password || password.length < 6) {
    return res.status(400).json({ error: 'Пароль от 6 символов' });
  }

  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    const hash = await bcrypt.hash(password, 10);
    const accRes = await client.query(
      'INSERT INTO accounts (nickname, email, password_hash, referral_nick) VALUES ($1, $2, $3, $4) RETURNING id, nickname',
      [nickname, email.toLowerCase(), hash, referral || null]
    );
    const account = accRes.rows[0];

    await client.query(
      'INSERT INTO characters (account_id, cash, bank_balance, level) VALUES ($1, 5000, 0, 1)',
      [account.id]
    );
    await client.query('COMMIT');

    const token = jwt.sign({ accountId: account.id, nickname: account.nickname }, JWT_SECRET, { expiresIn: '7d' });
    res.status(201).json({ token, nickname: account.nickname });
  } catch (err) {
    await client.query('ROLLBACK');
    if (err.code === '23505') {
      return res.status(409).json({ error: 'Ник или Email уже заняты' });
    }
    res.status(500).json({ error: 'Registration failed' });
  } finally {
    client.release();
  }
});

app.post('/api/auth/login', async (req, res) => {
  const { nickname, password } = req.body || {};
  if (!nickname || !password) {
    return res.status(400).json({ error: 'Укажите ник и пароль' });
  }

  try {
    const { rows } = await pool.query(
      'SELECT id, nickname, password_hash, is_banned FROM accounts WHERE nickname = $1 LIMIT 1',
      [nickname]
    );
    if (rows.length === 0) {
      return res.status(401).json({ error: 'Аккаунт не найден' });
    }
    const account = rows[0];
    if (account.is_banned) {
      return res.status(403).json({ error: 'Аккаунт заблокирован' });
    }

    const valid = await bcrypt.compare(password, account.password_hash);
    if (!valid) {
      return res.status(401).json({ error: 'Неверный пароль' });
    }

    const token = jwt.sign({ accountId: account.id, nickname: account.nickname }, JWT_SECRET, { expiresIn: '7d' });
    res.json({ token, nickname: account.nickname });
  } catch (err) {
    res.status(500).json({ error: 'Login failed' });
  }
});

app.get('/api/character/me', authMiddleware, async (req, res) => {
  try {
    const { rows } = await pool.query(
      `SELECT c.id, a.nickname, c.gender, c.skin_id, c.cash, c.bank_balance,
              c.level, c.exp, c.job_id, c.faction_id, c.faction_rank,
              c.wanted_stars, c.jail_seconds, c.pos_x, c.pos_y, c.pos_z
       FROM characters c
       JOIN accounts a ON a.id = c.account_id
       WHERE c.account_id = $1 LIMIT 1`,
      [req.user.accountId]
    );
    if (rows.length === 0) return res.status(404).json({ error: 'Character not found' });
    res.json({ character: rows[0] });
  } catch (err) {
    res.status(500).json({ error: 'Failed to load character' });
  }
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`CRMP API listening on port ${port}`));
