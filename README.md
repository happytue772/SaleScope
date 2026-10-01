# SaleScope

SaleScope는 온라인 리테일 거래 데이터를 Hadoop MapReduce로 집계하고, 집계 결과를 Oracle Database에 적재한 뒤 Servlet/JSP 기반 웹 화면에서 조회·시각화하는 매출 분석 프로젝트입니다.

이 저장소는 다음 두 Maven 프로젝트로 구성됩니다.

- `salescope-batch`: 월별·상품별 매출 집계 및 데이터 품질 카운터 생성
- `salescope-web`: 집계 결과 적재, Oracle 조회, 대시보드·검색·CSV 다운로드·시각화

## 주요 기능

### 배치 분석

- 온라인 리테일 CSV 파싱 및 거래 분류
- 월별 매출 MapReduce 집계
- 상품별 매출 MapReduce 집계
- 정상·취소·결측·오류 데이터 카운터 기록
- `part-r-*` 결과 및 `data_quality.tsv` 생성

### 웹 애플리케이션

- 데이터셋·분석 작업 선택형 대시보드
- 총매출, 순매출, 판매수량, 취소금액 요약
- 월별 매출 기간 조회 및 상세 결과
- 상품명·상품코드·매출 조건 검색
- 상품별 결과 CSV 다운로드
- TOP 5 상품과 월별 추이 시각화
- Hadoop 월별·상품별 결과를 Oracle에 적재

## 기술 구성

| 영역 | 기술 |
|---|---|
| Language | Java 8 (`salescope-batch`), Java 11 (`salescope-web`) |
| Batch | Hadoop MapReduce 2.5.1, Apache Commons CSV 1.9.0 |
| Web | Servlet 4.0.1, JSP, JSTL 1.2, JavaScript, CSS |
| Database | Oracle Database, JDBC (`ojdbc8` 19.3.0.0) |
| Build | Maven |
| Deployment | WAR 기반 Servlet Container |

## 처리 구조

```mermaid
flowchart LR
    CSV["거래 CSV"] --> Batch["Hadoop MapReduce"]
    Batch --> TSV["월별·상품별 TSV"]
    TSV --> Import["결과 적재 Servlet"]
    Import --> Oracle["Oracle Database"]
    Oracle --> Web["Servlet · JSP 대시보드"]
```

## 프로젝트 구조

```text
SaleScope/
├── salescope-batch/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/salescope/batch/
│       │   ├── monthly/
│       │   ├── parser/
│       │   ├── product/
│       │   └── util/
│       └── test/java/com/salescope/batch/parser/
├── salescope-web/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/salescope/
│       │   ├── controller/
│       │   ├── dao/
│       │   ├── dto/
│       │   ├── service/
│       │   └── util/
│       ├── resources/
│       └── webapp/
│           ├── WEB-INF/views/
│           └── assets/
├── .gitignore
└── README.md
```

## 웹 경로

| 경로 | 방식 | 기능 |
|---|---|---|
| `/dashboard` | GET | 데이터셋·분석 작업별 요약 대시보드 |
| `/monthly-sales` | GET | 월별 매출 조회 |
| `/product-sales` | GET | 상품별 매출 검색·정렬·페이징 |
| `/product-sales/csv` | GET | 상품별 조회 결과 CSV 다운로드 |
| `/visualization` | GET | 상품 TOP 5 및 월별 추이 시각화 |
| `/result/import` | POST | 월별·상품별 Hadoop 결과 적재 |

## 데이터베이스 설정

실제 DB 비밀번호가 포함된 `db.properties`는 저장소에서 제외합니다.

1. 예시 파일을 복사합니다.

```powershell
Copy-Item `
  salescope-web\src\main\resources\db.properties.example `
  salescope-web\src\main\resources\db.properties
```

2. 로컬 환경에 맞게 값을 수정합니다.

```properties
db.driver=oracle.jdbc.OracleDriver
db.url=jdbc:oracle:thin:@//localhost:1521/orcl
db.user=SALESCOPE
db.password=로컬_DB_비밀번호
```

`DBConnectionUtil`은 클래스패스의 `db.properties`를 UTF-8로 읽으며, 네 개 설정값이 모두 존재해야 연결을 생성합니다.

웹 프로젝트가 조회·적재하는 Oracle 테이블은 소스 기준으로 다음과 같습니다.

- `DATASET`
- `ANALYSIS_JOB`
- `MONTHLY_SALES_STAT`
- `DATA_QUALITY_STAT`
- `PRODUCT`
- `PRODUCT_SALES_STAT`

## 빌드

### 웹 애플리케이션

```powershell
Set-Location salescope-web
mvn clean package
```

생성 결과:

```text
salescope-web/target/salescope-web.war
```

### Hadoop 배치

```powershell
Set-Location salescope-batch
mvn clean package
```

생성 결과:

```text
salescope-batch/target/salescope-batch.jar
```

`salescope-batch`는 Maven Shade Plugin을 사용하며 Hadoop 의존성은 실행 환경에서 제공되는 것으로 설정되어 있습니다.

## 배치 실행 인자

두 Driver는 동일하게 `jobId`, 입력 경로, 출력 경로, 선택적 예상 행 수를 받습니다.

```text
MonthlySalesDriver <jobId> <input> <output> [expectedRows]
ProductSalesDriver <jobId> <input> <output> [expectedRows]
```

- `jobId`: 1 이상의 정수
- `input`: Hadoop 입력 경로
- `output`: 기존에 존재하지 않는 Hadoop 출력 경로
- `expectedRows`: 선택값이며 0 이상의 예상 데이터 행 수

## 실행 시 주의사항

- 웹 프로젝트는 Java 11 기준입니다.
- 배치 프로젝트는 Hadoop 2.5.1 호환을 위해 Java 8 기준입니다.
- 웹 애플리케이션 실행 전 Oracle 데이터베이스와 대상 PDB가 열린 상태여야 합니다.
- Hadoop 출력 경로가 이미 존재하면 MapReduce 작업이 실패하므로 새 출력 경로를 사용해야 합니다.
- 실제 DB 계정 정보와 원본 대용량 데이터는 GitHub에 커밋하지 않습니다.

## 현재 저장소 범위

이 저장소는 업로드된 최종 소스에 포함된 Java, JSP, JavaScript, CSS, Maven 및 Eclipse 프로젝트 설정을 유지합니다. Maven 빌드 산출물과 로컬 DB 비밀번호만 버전 관리 대상에서 제외합니다.
