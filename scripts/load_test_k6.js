import http from 'k6/http';
import { check, sleep } from 'k6';

// Конфигурация сценария нагрузки
export const options = {
  stages: [
    { duration: '30s', target: 20 },  // Разгон до 20 виртуальных пользователей
    { duration: '1m', target: 50 },   // Удержание нагрузки 50 пользователей
    { duration: '30s', target: 100 }, // Пиковая нагрузка 100 пользователей
    { duration: '30s', target: 0 },   // Плавное снижение до 0
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% запросов должны выполняться быстрее 500мс
    http_req_failed: ['rate<0.01'],   // Ошибок должно быть меньше 1%
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  // 1. Запрос списка опубликованных статей (чтение — наиболее частая операция)
  const articlesRes = http.get(`${BASE_URL}/api/articles?size=10`);
  check(articlesRes, {
    'GET /api/articles status is 200 or 401': (r) => r.status === 200 || r.status === 401,
    'GET /api/articles response time < 300ms': (r) => r.timings.duration < 300,
  });

  // 2. Запрос JWKS ключей авторизации
  const jwksRes = http.get(`${BASE_URL}/.well-known/jwks.json`);
  check(jwksRes, {
    'JWKS status is 200': (r) => r.status === 200,
  });

  // Имитация паузы реального пользователя между запросами
  sleep(1);
}
