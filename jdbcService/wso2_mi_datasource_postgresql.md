# WSO2 MI PostgreSQL 데이터소스 최적화 가이드

## 1. 권장 설정 요약

### 1.1 기본 정보
- **DB 타입**: PostgreSQL
- **용도**: JDBC Execution Framework
- **예상 트래픽**: 중간~높음

### 1.2 설정값 비교

| 설정 항목 | 기존값 | 권장값 | 설명 |
|----------|--------|--------|------|
| **Connection Pool** | | | |
| maxActive | 50 | 50 | ✅ 적절함 |
| maxIdle | 10 | 20 | ⬆️ 증가 권장 |
| minIdle | 5 | 10 | ⬆️ 증가 권장 |
| initialSize | - | 10 | ➕ 추가 권장 |
| **Validation** | | | |
| testOnBorrow | - | true | ➕ 추가 필수 |
| testWhileIdle | - | true | ➕ 추가 필수 |
| validationQuery | - | SELECT 1 | ➕ 추가 필수 |
| validationInterval | - | 30000 | ➕ 추가 권장 |
| **Timeout** | | | |
| maxWait | - | 30000 | ➕ 추가 필수 |
| connectTimeout | - | 30 (URL) | ➕ 추가 권장 |
| socketTimeout | - | 60 (URL) | ➕ 추가 권장 |
| **Eviction** | | | |
| timeBetweenEvictionRunsMillis | - | 30000 | ➕ 추가 권장 |
| minEvictableIdleTimeMillis | - | 600000 | ➕ 추가 권장 |
| **Leak Detection** | | | |
| removeAbandoned | - | true | ➕ 추가 권장 |
| removeAbandonedTimeout | - | 300 | ➕ 추가 권장 |

---

## 2. 상세 설정 설명

### 2.1 Connection Pool 설정

#### maxActive (최대 활성 연결 수)
```xml
<maxActive>50</maxActive>
```
- **현재값**: 50
- **권장값**: 50
- **설명**: 동시 최대 연결 수
- **계산 공식**: 
  ```
  maxActive = (예상 동시 요청 수 × 1.2) + 버퍼(10)
  
  예) 동시 요청 40개 예상
      → (40 × 1.2) + 10 = 58 → 50으로 설정 (적절)
  ```

#### maxIdle (최대 유휴 연결 수)
```xml
<maxIdle>20</maxIdle>
```
- **기존값**: 10
- **권장값**: 20
- **변경 이유**: 
  - 트래픽 급증 시 빠른 대응
  - Connection 생성 오버헤드 감소
  - maxActive의 40% 유지 권장

#### minIdle (최소 유휴 연결 수)
```xml
<minIdle>10</minIdle>
```
- **기존값**: 5
- **권장값**: 10
- **변경 이유**:
  - 기본 트래픽 처리를 위한 충분한 연결 유지
  - Cold Start 방지
  - maxActive의 20% 유지 권장

#### initialSize (초기 연결 수)
```xml
<initialSize>10</initialSize>
```
- **기존값**: 없음
- **권장값**: 10
- **추가 이유**:
  - 서버 시작 시 즉시 연결 확보
  - 첫 번째 요청 지연 방지
  - minIdle과 동일하게 설정 권장

---

### 2.2 Connection Validation (연결 검증)

#### testOnBorrow
```xml
<testOnBorrow>true</testOnBorrow>
```
- **권장값**: true
- **설명**: Pool에서 연결을 가져올 때마다 검증
- **장점**: 끊어진 연결 사용 방지
- **단점**: 약간의 성능 오버헤드 (validationInterval로 완화)

#### testWhileIdle
```xml
<testWhileIdle>true</testWhileIdle>
```
- **권장값**: true
- **설명**: 유휴 상태 연결을 주기적으로 검증
- **장점**: 장시간 미사용 연결 자동 제거

#### validationQuery
```xml
<validationQuery>SELECT 1</validationQuery>
```
- **PostgreSQL 전용**: `SELECT 1`
- **Oracle 전용**: `SELECT 1 FROM DUAL`
- **MSSQL 전용**: `SELECT 1`
- **설명**: 가장 가벼운 쿼리로 연결 상태 확인

#### validationInterval
```xml
<validationInterval>30000</validationInterval>
```
- **권장값**: 30000 (30초)
- **설명**: 검증 간격 (밀리초)
- **효과**: 
  - 동일 연결을 30초 내 재검증하지 않음
  - testOnBorrow의 성능 오버헤드 감소

---

### 2.3 Timeout 설정

#### maxWait (연결 대기 시간)
```xml
<maxWait>30000</maxWait>
```
- **권장값**: 30000 (30초)
- **설명**: Pool에서 연결을 기다리는 최대 시간
- **동작**: 
  - 30초 내 연결 확보 실패 시 SQLException 발생
  - 무한 대기 방지

#### URL 파라미터: connectTimeout
```xml
<url>jdbc:postgresql://...?connectTimeout=30</url>
```
- **권장값**: 30 (초)
- **설명**: DB 서버 연결 시도 타임아웃
- **동작**: 네트워크 장애 시 빠른 실패

#### URL 파라미터: socketTimeout
```xml
<url>jdbc:postgresql://...?socketTimeout=60</url>
```
- **권장값**: 60 (초)
- **설명**: 쿼리 실행 타임아웃
- **동작**: 
  - 쿼리 응답 없을 시 60초 후 종료
  - 무한 대기 방지

#### URL 파라미터: tcpKeepAlive
```xml
<url>jdbc:postgresql://...?tcpKeepAlive=true</url>
```
- **권장값**: true
- **설명**: TCP Keep-Alive 활성화
- **효과**: 방화벽/로드밸런서에서 연결 끊김 방지

---

### 2.4 Idle Connection Eviction (유휴 연결 제거)

#### timeBetweenEvictionRunsMillis
```xml
<timeBetweenEvictionRunsMillis>30000</timeBetweenEvictionRunsMillis>
```
- **권장값**: 30000 (30초)
- **설명**: 유휴 연결 검사 주기
- **동작**: 30초마다 Eviction 스레드 실행

#### minEvictableIdleTimeMillis
```xml
<minEvictableIdleTimeMillis>600000</minEvictableIdleTimeMillis>
```
- **권장값**: 600000 (10분)
- **설명**: 연결을 제거할 최소 유휴 시간
- **동작**: 10분 이상 사용 안 된 연결 제거

#### numTestsPerEvictionRun
```xml
<numTestsPerEvictionRun>3</numTestsPerEvictionRun>
```
- **권장값**: 3
- **설명**: 한 번에 검사할 연결 수
- **효과**: 과도한 검사 방지

---

### 2.5 Connection Leak Detection (연결 누수 탐지)

#### removeAbandoned
```xml
<removeAbandoned>true</removeAbandoned>
```
- **권장값**: true
- **설명**: 누수된 연결 자동 회수
- **중요**: 프로덕션 환경 필수

#### removeAbandonedTimeout
```xml
<removeAbandonedTimeout>300</removeAbandonedTimeout>
```
- **권장값**: 300 (5분)
- **설명**: 연결을 누수로 판단할 시간
- **동작**: 5분 이상 반환 안 된 연결 강제 회수

#### logAbandoned
```xml
<logAbandoned>true</logAbandoned>
```
- **권장값**: true
- **설명**: 누수 발생 시 스택 트레이스 로깅
- **효과**: 개발 단계 디버깅에 유용

---

### 2.6 Transaction 설정

#### defaultAutoCommit
```xml
<defaultAutoCommit>false</defaultAutoCommit>
```
- **권장값**: false
- **설명**: 자동 커밋 비활성화
- **이유**: 
  - JDBC Framework에서 명시적 트랜잭션 제어
  - `commit_action: COMMIT/CONTINUE` 설정 활용

#### defaultTransactionIsolation
```xml
<defaultTransactionIsolation>READ_COMMITTED</defaultTransactionIsolation>
```
- **권장값**: READ_COMMITTED
- **설명**: PostgreSQL 기본값
- **레벨**:
  - `READ_UNCOMMITTED`: 미지원 (PostgreSQL)
  - `READ_COMMITTED`: ✅ 권장
  - `REPEATABLE_READ`: 동시성 저하
  - `SERIALIZABLE`: 성능 저하

---

### 2.7 Performance Optimization

#### poolPreparedStatements
```xml
<poolPreparedStatements>false</poolPreparedStatements>
```
- **권장값**: false
- **이유**: 
  - PostgreSQL 드라이버가 자체적으로 PreparedStatement 캐싱
  - 중복 캐싱 방지

#### jdbcInterceptors
```xml
<jdbcInterceptors>ConnectionState;StatementFinalizer;ResetAbandonedTimer</jdbcInterceptors>
```
- **ConnectionState**: 연결 상태 추적
- **StatementFinalizer**: Statement 자동 종료
- **ResetAbandonedTimer**: 타이머 리셋

---

## 3. PostgreSQL 특화 URL 파라미터

### 3.1 전체 URL 예시
```xml
<url>jdbc:postgresql://70.220.112.34:2866/SCISDEV?ApplicationName=WSO2MI&amp;connectTimeout=30&amp;socketTimeout=60&amp;tcpKeepAlive=true</url>
```

### 3.2 파라미터 설명

| 파라미터 | 값 | 설명 |
|---------|-----|------|
| ApplicationName | WSO2MI | PostgreSQL 로그에 표시될 애플리케이션 이름 |
| connectTimeout | 30 | 연결 타임아웃 (초) |
| socketTimeout | 60 | 쿼리 타임아웃 (초) |
| tcpKeepAlive | true | TCP Keep-Alive 활성화 |

### 3.3 추가 고려 파라미터 (선택)

```
?ApplicationName=WSO2MI
&connectTimeout=30
&socketTimeout=60
&tcpKeepAlive=true
&loginTimeout=30
&prepareThreshold=5
&defaultRowFetchSize=100
&ssl=false
```

---

## 4. 모니터링 지표

### 4.1 확인해야 할 메트릭

```sql
-- 현재 활성 연결 수
SELECT count(*) 
FROM pg_stat_activity 
WHERE datname = 'SCISDEV' AND application_name = 'WSO2MI';

-- 연결 상태별 분포
SELECT state, count(*) 
FROM pg_stat_activity 
WHERE datname = 'SCISDEV' AND application_name = 'WSO2MI'
GROUP BY state;

-- 장시간 실행 쿼리
SELECT pid, now() - query_start AS duration, query 
FROM pg_stat_activity 
WHERE datname = 'SCISDEV' AND application_name = 'WSO2MI'
  AND state = 'active'
  AND now() - query_start > interval '1 minute';
```

### 4.2 WSO2 MI 로그 확인

```bash
# Connection Pool 상태
tail -f wso2mi.log | grep -i "pool"

# Connection Leak
tail -f wso2mi.log | grep -i "abandoned"

# SQL Exception
tail -f wso2mi.log | grep -i "SQLException"
```

---

## 5. 환경별 권장값

### 5.1 개발 환경
```xml
<maxActive>20</maxActive>
<maxIdle>10</maxIdle>
<minIdle>5</minIdle>
<initialSize>5</initialSize>
<removeAbandonedTimeout>60</removeAbandonedTimeout>
```

### 5.2 테스트 환경
```xml
<maxActive>30</maxActive>
<maxIdle>15</maxIdle>
<minIdle>8</minIdle>
<initialSize>8</initialSize>
<removeAbandonedTimeout>180</removeAbandonedTimeout>
```

### 5.3 운영 환경 (현재 권장)
```xml
<maxActive>50</maxActive>
<maxIdle>20</maxIdle>
<minIdle>10</minIdle>
<initialSize>10</initialSize>
<removeAbandonedTimeout>300</removeAbandonedTimeout>
```

### 5.4 고트래픽 환경
```xml
<maxActive>100</maxActive>
<maxIdle>40</maxIdle>
<minIdle>20</minIdle>
<initialSize>20</initialSize>
<removeAbandonedTimeout>300</removeAbandonedTimeout>
```

---

## 6. PostgreSQL 서버 설정 확인

### 6.1 max_connections 확인
```sql
SHOW max_connections;
-- 결과: 일반적으로 100~200
```

**권장 관계:**
```
WSO2 MI maxActive ≤ PostgreSQL max_connections × 0.7
```

예) PostgreSQL max_connections = 100이면
    WSO2 MI maxActive ≤ 70

### 6.2 idle_in_transaction_session_timeout
```sql
SHOW idle_in_transaction_session_timeout;
-- 권장: 300000 (5분)
```

### 6.3 statement_timeout
```sql
SHOW statement_timeout;
-- 권장: 60000 (1분)
```

---

## 7. 문제 해결 가이드

### 7.1 "Too many connections" 에러

**증상:**
```
FATAL: sorry, too many clients already
```

**해결:**
1. PostgreSQL `max_connections` 증가
2. WSO2 MI `maxActive` 감소
3. Connection Leak 확인

### 7.2 Connection Timeout

**증상:**
```
Timeout waiting for connection from pool
```

**해결:**
1. `maxActive` 증가
2. `maxWait` 증가
3. 쿼리 성능 최적화

### 7.3 Connection Leak

**증상:**
```
Connection abandoned, removing
```

**해결:**
1. 코드에서 Connection 명시적 종료 확인
2. `removeAbandonedTimeout` 조정
3. 로그 분석으로 원인 파악

---

## 8. 체크리스트

### 배포 전 확인사항
- [ ] maxActive ≤ PostgreSQL max_connections × 0.7
- [ ] testOnBorrow = true
- [ ] validationQuery 설정됨
- [ ] maxWait 설정됨 (30초)
- [ ] removeAbandoned = true
- [ ] defaultAutoCommit = false
- [ ] URL에 tcpKeepAlive=true 추가
- [ ] ApplicationName 설정됨

### 배포 후 확인사항
- [ ] Connection Pool 정상 초기화
- [ ] 첫 요청 정상 처리
- [ ] PostgreSQL 연결 수 정상
- [ ] 로그에 에러 없음
- [ ] 모니터링 지표 정상

---

## 9. 참고 자료

### WSO2 MI 공식 문서
- https://mi.docs.wso2.com/en/latest/install-and-setup/setup/databases/

### PostgreSQL JDBC 드라이버
- https://jdbc.postgresql.org/documentation/

### Tomcat JDBC Connection Pool
- https://tomcat.apache.org/tomcat-9.0-doc/jdbc-pool.html

---

**작성일**: 2026-01-29  
**버전**: 1.0  
**대상 환경**: WSO2 MI + PostgreSQL
