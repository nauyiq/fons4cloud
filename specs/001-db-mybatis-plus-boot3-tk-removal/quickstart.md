# Quickstart: db 模块 MyBatis-Plus Boot3 Starter 迁移与 TK Mapper 移除验证

**日期**: 2026-04-30  
**SDD Level**: S2  
**用途**: 复现本次补救验证步骤。

## 1. 设置 JDK 21

当前项目使用 Java 21。若 Maven 默认绑定 JDK 8，会出现 `无效的目标发行版: 21`。

```powershell
$env:JAVA_HOME='C:\hongqy\C\Java\jdk21'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -version
javac -version
```

## 2. 构建 db-core

```powershell
C:\hongqy\tool\apache-maven-3.9.8\bin\mvn.cmd -pl fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-core -am -DskipTests package
```

期望结果：Reactor 构建成功。

## 3. 构建 db-shardingsphere 下游链路

```powershell
C:\hongqy\tool\apache-maven-3.9.8\bin\mvn.cmd -pl fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-shardingsphere -am -DskipTests package
```

期望结果：root、common、common-base、common-util、common-db、db-core、db-datasource、db-shardingsphere 均成功。

## 4. 检查依赖树

```powershell
C:\hongqy\tool\apache-maven-3.9.8\bin\mvn.cmd -pl fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-core -am dependency:tree | Select-String -Pattern 'mybatis-plus|tk.mybatis|mapper-spring-boot-starter|mybatis-spring'
```

期望结果：

- 存在 `org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3`
- 存在 `org.mybatis:mybatis-spring:3.0.3`
- 存在 `com.baomidou:mybatis-plus-spring-boot3-starter:3.5.5`
- 不存在 `tk.mybatis`

## 5. 检查源码残留

```powershell
Get-ChildItem -Path . -Recurse -File -Exclude '.flattened-pom.xml' |
    Where-Object { $_.FullName -notmatch '\\target\\' } |
    Select-String -Pattern 'tk.mybatis|com.fons.cloud.db.tk|BaseTkMapper|BaseTkService|PrimaryLessTk|javax.persistence|mapper-spring-boot-starter|mybatis-plus-boot-starter'
```

期望结果：无源码残留输出。

## 6. 残余风险

- 上述构建命令使用 `-DskipTests`，没有执行自动化测试。
- 仓库中可能存在构建产生的 `.flattened-pom.xml` 和 `target/` 未跟踪文件，不属于本次源码变更。
- 未纳入构建的业务模块若引用 `com.fons.cloud.db.tk`，会在后续编译时暴露。

