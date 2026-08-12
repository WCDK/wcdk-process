-- ============================================
-- WCDK Process 流程引擎数据库表结构
-- 数据库: MySQL
-- 版本: 1.0.0
-- 字符集: utf8mb4
-- ============================================

-- -------------------------------------------
-- 1. 流程定义表
-- 存储BPMN流程定义信息，支持版本控制
-- -------------------------------------------
CREATE TABLE wcdk_process_definition (
    id                      VARCHAR(64)     NOT NULL,           -- 主键ID
    tenant_id               VARCHAR(64)     NOT NULL DEFAULT 'default', -- 租户ID
    `key`                   VARCHAR(128)    NOT NULL,           -- 流程定义Key
    name                    VARCHAR(256),                       -- 流程定义名称
    version                 INT             NOT NULL DEFAULT 1, -- 版本号
    category                VARCHAR(128),                       -- 流程分类
    description             VARCHAR(1024),                      -- 流程描述
    deployment_id           VARCHAR(64),                        -- 部署ID
    resource_name           VARCHAR(256),                       -- 资源名称
    diagram_resource_name   VARCHAR(256),                       -- 流程图资源名称
    graph_json              TEXT,                               -- 流程图JSON（缓存）
    suspended               TINYINT         NOT NULL DEFAULT 0, -- 是否挂起（0-正常 1-挂起）
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时间
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程定义表';

-- 唯一索引：流程定义Key+版本号唯一
CREATE UNIQUE INDEX uk_process_def_key_version ON wcdk_process_definition(tenant_id, `key`, version);
-- 索引：按部署ID查询
CREATE INDEX idx_process_def_deployment ON wcdk_process_definition(deployment_id);

-- -------------------------------------------
-- 2. 部署信息表
-- 存储流程部署的元数据信息
-- -------------------------------------------
CREATE TABLE wcdk_deployment (
    id                      VARCHAR(64)     NOT NULL,           -- 主键ID
    tenant_id               VARCHAR(64)     NOT NULL DEFAULT 'default', -- 租户ID
    name                    VARCHAR(256),                       -- 部署名称
    category                VARCHAR(128),                       -- 部署分类
    deployment_time         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 部署时间
    source_system           VARCHAR(128),                       -- 来源系统
    description             VARCHAR(1024),                      -- 部署描述
    version                 INT             NOT NULL DEFAULT 1, -- 版本号
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时间
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部署信息表';

-- -------------------------------------------
-- 3. BPMN资源表
-- 存储BPMN XML资源文件
-- -------------------------------------------
CREATE TABLE wcdk_resource (
    id                      VARCHAR(64)     NOT NULL,           -- 主键ID
    deployment_id           VARCHAR(64)     NOT NULL,           -- 部署ID
    tenant_id               VARCHAR(64)     NOT NULL DEFAULT 'default', -- 租户ID
    name                    VARCHAR(256)    NOT NULL,           -- 资源名称
    resource_type           VARCHAR(32)     NOT NULL,           -- 资源类型（BPMN/XML/JSON）
    content                 MEDIUMTEXT,                         -- 文本内容
    content_bytes           MEDIUMBLOB,                         -- 二进制内容
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BPMN资源表';

-- 索引：按部署ID查询资源
CREATE INDEX idx_resource_deployment ON wcdk_resource(deployment_id);

-- -------------------------------------------
-- 4. 流程实例表（流程聚合根）
-- 存储运行中的流程实例信息
-- -------------------------------------------
CREATE TABLE wcdk_process_instance (
    id                      VARCHAR(64)     NOT NULL,           -- 主键ID
    tenant_id               VARCHAR(64)     NOT NULL DEFAULT 'default', -- 租户ID
    process_definition_id   VARCHAR(64)     NOT NULL,           -- 流程定义ID
    process_definition_key  VARCHAR(128)    NOT NULL,           -- 流程定义Key
    process_definition_version INT          NOT NULL,           -- 流程定义版本
    business_key            VARCHAR(256),                       -- 业务KEY
    parent_process_instance_id VARCHAR(64),                     -- 父流程实例ID
    root_process_instance_id VARCHAR(64),                       -- 根流程实例ID
    starter                 VARCHAR(128),                       -- 发起人
    start_time              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 开始时间
    end_time                TIMESTAMP       NULL,               -- 结束时间
    duration_ms             BIGINT,                             -- 执行时长（毫秒）
    status                  VARCHAR(32)     NOT NULL DEFAULT 'RUNNING', -- 状态（RUNNING/COMPLETED/TERMINATED/SUSPENDED）
    suspension_state        INT             NOT NULL DEFAULT 1, -- 挂起状态（1-激活 2-挂起）
    error_message           TEXT,                               -- 错误信息
    revision                BIGINT          NOT NULL DEFAULT 1, -- 乐观锁版本号
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时间
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程实例表';

-- 索引：按流程定义ID查询
CREATE INDEX idx_pi_process_def ON wcdk_process_instance(process_definition_id);
-- 索引：按业务KEY查询
CREATE INDEX idx_pi_business_key ON wcdk_process_instance(tenant_id, business_key);
-- 索引：按发起人查询
CREATE INDEX idx_pi_starter ON wcdk_process_instance(starter);
-- 索引：按父流程实例查询
CREATE INDEX idx_pi_parent ON wcdk_process_instance(parent_process_instance_id);
-- 索引：按状态查询
CREATE INDEX idx_pi_status ON wcdk_process_instance(status);

-- -------------------------------------------
-- 5. 执行实例表（BPMN Token / Scope）
-- 存储流程执行的Token和作用域信息
-- -------------------------------------------
CREATE TABLE wcdk_execution (
    id                      VARCHAR(64)     NOT NULL,           -- 主键ID
    tenant_id               VARCHAR(64)     NOT NULL DEFAULT 'default', -- 租户ID
    process_instance_id     VARCHAR(64)     NOT NULL,           -- 流程实例ID
    process_definition_id   VARCHAR(64)     NOT NULL,           -- 流程定义ID
    parent_id               VARCHAR(64),                        -- 父执行实例ID
    scope_execution_id      VARCHAR(64),                        -- 作用域执行实例ID
    root_execution_id       VARCHAR(64),                        -- 根执行实例ID
    node_id                 VARCHAR(128)    NOT NULL,           -- 节点ID
    node_type               VARCHAR(64)     NOT NULL,           -- 节点类型
    state                   VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE', -- 状态（ACTIVE/WAITING/COMPLETED/TERMINATED）
    is_scope                TINYINT         NOT NULL DEFAULT 0, -- 是否为作用域（0-否 1-是）
    is_concurrent           TINYINT         NOT NULL DEFAULT 0, -- 是否并发（0-否 1-是）
    is_event_scope          TINYINT         NOT NULL DEFAULT 0, -- 是否为事件作用域（0-否 1-是）
    is_multi_instance       TINYINT         NOT NULL DEFAULT 0, -- 是否多实例（0-否 1-是）
    multi_instance_index    INT,                                -- 多实例索引
    multi_instance_total    INT,                                -- 多实例总数
    suspension_state        INT             NOT NULL DEFAULT 1, -- 挂起状态（1-激活 2-挂起）
    revision                BIGINT          NOT NULL DEFAULT 1, -- 乐观锁版本号
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时间
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='执行实例表';

-- 索引：按流程实例ID查询
CREATE INDEX idx_exec_process_instance ON wcdk_execution(process_instance_id);
-- 索引：按父执行实例ID查询
CREATE INDEX idx_exec_parent ON wcdk_execution(parent_id);
-- 索引：按作用域执行实例ID查询
CREATE INDEX idx_exec_scope ON wcdk_execution(scope_execution_id);
-- 索引：按节点ID查询
CREATE INDEX idx_exec_node ON wcdk_execution(node_id);
-- 索引：按状态查询
CREATE INDEX idx_exec_state ON wcdk_execution(state);

-- -------------------------------------------
-- 6. 任务表
-- 存储用户任务信息
-- -------------------------------------------
CREATE TABLE wcdk_task (
    id                      VARCHAR(64)     NOT NULL,           -- 主键ID
    tenant_id               VARCHAR(64)     NOT NULL DEFAULT 'default', -- 租户ID
    process_instance_id     VARCHAR(64)     NOT NULL,           -- 流程实例ID
    execution_id            VARCHAR(64)     NOT NULL,           -- 执行实例ID
    process_definition_id   VARCHAR(64)     NOT NULL,           -- 流程定义ID
    task_definition_key     VARCHAR(128),                       -- 任务定义Key
    name                    VARCHAR(256),                       -- 任务名称
    state                   VARCHAR(32)     NOT NULL DEFAULT 'CREATED', -- 状态（CREATED/CLAIMED/COMPLETED/CANCELLED）
    assignee                VARCHAR(128),                       -- 处理人
    owner                   VARCHAR(128),                       -- 拥有者
    priority                INT             NOT NULL DEFAULT 50, -- 优先级（0-100）
    due_time                TIMESTAMP       NULL,               -- 到期时间
    create_time             TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    claim_time              TIMESTAMP       NULL,               -- 认领时间
    complete_time           TIMESTAMP       NULL,               -- 完成时间
    form_data               TEXT,                               -- 表单数据（JSON）
    description             VARCHAR(1024),                      -- 任务描述
    revision                BIGINT          NOT NULL DEFAULT 1, -- 乐观锁版本号
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时间
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- 索引：按流程实例ID查询任务
CREATE INDEX idx_task_process_instance ON wcdk_task(process_instance_id);
-- 索引：按执行实例ID查询任务
CREATE INDEX idx_task_execution ON wcdk_task(execution_id);
-- 索引：按处理人查询任务
CREATE INDEX idx_task_assignee ON wcdk_task(assignee);
-- 索引：按状态查询任务
CREATE INDEX idx_task_state ON wcdk_task(state);
-- 索引：按任务定义Key查询
CREATE INDEX idx_task_def_key ON wcdk_task(task_definition_key);
-- 索引：按创建时间查询
CREATE INDEX idx_task_create_time ON wcdk_task(create_time);

-- -------------------------------------------
-- 7. 任务参与者关系表
-- 存储任务的候选人和候选组信息
-- -------------------------------------------
CREATE TABLE wcdk_identity_link (
    id                      VARCHAR(64)     NOT NULL,           -- 主键ID
    tenant_id               VARCHAR(64)     NOT NULL DEFAULT 'default', -- 租户ID
    task_id                 VARCHAR(64)     NOT NULL,           -- 任务ID
    process_instance_id     VARCHAR(64)     NOT NULL,           -- 流程实例ID
    link_type               VARCHAR(32)     NOT NULL,           -- 关系类型（ASSIGNEE/CANDIDATE/OWNER/NOTIFY）
    user_id                 VARCHAR(128),                       -- 用户ID
    group_id                VARCHAR(128),                       -- 用户组ID
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务参与者关系表';

-- 索引：按任务ID查询
CREATE INDEX idx_il_task ON wcdk_identity_link(task_id);
-- 索引：按流程实例ID查询
CREATE INDEX idx_il_process_instance ON wcdk_identity_link(process_instance_id);
-- 索引：按用户ID查询
CREATE INDEX idx_il_user ON wcdk_identity_link(user_id);
-- 索引：按用户组ID查询
CREATE INDEX idx_il_group ON wcdk_identity_link(group_id);

-- -------------------------------------------
-- 8. 变量表（一个变量 = 一行）
-- 存储流程变量、执行变量、任务变量
-- -------------------------------------------
CREATE TABLE wcdk_variable (
    id                      VARCHAR(64)     NOT NULL,           -- 主键ID
    tenant_id               VARCHAR(64)     NOT NULL DEFAULT 'default', -- 租户ID
    process_instance_id     VARCHAR(64)     NOT NULL,           -- 流程实例ID
    scope_type              VARCHAR(32)     NOT NULL,           -- 作用域类型（PROCESS/EXECUTION/TASK）
    scope_id                VARCHAR(64)     NOT NULL,           -- 作用域ID
    name                    VARCHAR(128)    NOT NULL,           -- 变量名
    value_type              VARCHAR(32)     NOT NULL,           -- 值类型（STRING/LONG/DOUBLE/BOOLEAN/JSON/NULL）
    value_text              TEXT,                               -- 文本值
    value_long              BIGINT,                             -- 长整型值
    value_double            DOUBLE,                             -- 浮点型值
    value_boolean           TINYINT,                            -- 布尔型值（0-否 1-是）
    revision                BIGINT          NOT NULL DEFAULT 1, -- 乐观锁版本号
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时间
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='变量表';

-- 唯一索引：同一作用域下变量名唯一
CREATE UNIQUE INDEX uk_variable_scope ON wcdk_variable(scope_type, scope_id, name);
-- 索引：按流程实例ID查询
CREATE INDEX idx_var_process_instance ON wcdk_variable(process_instance_id);
-- 索引：按变量名查询
CREATE INDEX idx_var_name ON wcdk_variable(name);

-- -------------------------------------------
-- 9. 事件订阅表
-- 存储消息、信号、定时器等事件订阅
-- -------------------------------------------
CREATE TABLE wcdk_event_subscription (
    id                      VARCHAR(64)     NOT NULL,           -- 主键ID
    tenant_id               VARCHAR(64)     NOT NULL DEFAULT 'default', -- 租户ID
    process_instance_id     VARCHAR(64)     NOT NULL,           -- 流程实例ID
    execution_id            VARCHAR(64)     NOT NULL,           -- 执行实例ID
    event_type              VARCHAR(32)     NOT NULL,           -- 事件类型（MESSAGE/SIGNAL/TIMER/ERROR）
    event_name              VARCHAR(128)    NOT NULL,           -- 事件名称
    node_id                 VARCHAR(128)    NOT NULL,           -- 节点ID
    state                   VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE', -- 状态（ACTIVE/DELETED）
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事件订阅表';

-- 索引：按流程实例ID查询
CREATE INDEX idx_es_process_instance ON wcdk_event_subscription(process_instance_id);
-- 索引：按执行实例ID查询
CREATE INDEX idx_es_execution ON wcdk_event_subscription(execution_id);
-- 索引：按事件类型和名称查询
CREATE INDEX idx_es_event ON wcdk_event_subscription(event_type, event_name);
-- 索引：按状态查询
CREATE INDEX idx_es_state ON wcdk_event_subscription(state);

-- -------------------------------------------
-- 10. 定时任务表
-- 存储异步任务、定时器、外部任务等
-- -------------------------------------------
CREATE TABLE wcdk_job (
    id                      VARCHAR(64)     NOT NULL,           -- 主键ID
    tenant_id               VARCHAR(64)     NOT NULL DEFAULT 'default', -- 租户ID
    process_instance_id     VARCHAR(64),                        -- 流程实例ID
    execution_id            VARCHAR(64),                        -- 执行实例ID
    process_definition_id   VARCHAR(64),                        -- 流程定义ID
    node_id                 VARCHAR(128),                       -- 节点ID
    job_type                VARCHAR(32)     NOT NULL,           -- 任务类型（ASYNC_CONTINUE/TIMER/SERVICE_TASK/EXTERNAL_TASK/HISTORY/OUTBOX_RETRY）
    status                  VARCHAR(32)     NOT NULL DEFAULT 'READY', -- 状态（READY/CLAIMED/RETRY/DONE/DEAD/SUSPENDED）
    due_at                  TIMESTAMP       NULL,               -- 到期时间
    retry_count             INT             NOT NULL DEFAULT 0, -- 重试次数
    max_retries             INT             NOT NULL DEFAULT 3, -- 最大重试次数
    lock_owner              VARCHAR(128),                       -- 锁持有者
    lock_token              VARCHAR(256),                       -- 锁令牌
    lock_until              TIMESTAMP       NULL,               -- 锁到期时间
    payload                 TEXT,                               -- 任务负载（JSON）
    last_error              TEXT,                               -- 最后错误信息
    revision                BIGINT          NOT NULL DEFAULT 1, -- 乐观锁版本号
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时间
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时任务表';

-- 索引：按流程实例ID查询
CREATE INDEX idx_job_process_instance ON wcdk_job(process_instance_id);
-- 索引：按状态查询
CREATE INDEX idx_job_status ON wcdk_job(status);
-- 索引：按到期时间查询
CREATE INDEX idx_job_due_at ON wcdk_job(due_at);
-- 索引：按锁持有者查询
CREATE INDEX idx_job_lock_owner ON wcdk_job(lock_owner);
-- 索引：按任务类型查询
CREATE INDEX idx_job_type ON wcdk_job(job_type);

-- -------------------------------------------
-- 11. 并行网关汇合状态表
-- 存储并行网关Join的汇合状态
-- -------------------------------------------
CREATE TABLE wcdk_join_state (
    id                      VARCHAR(64)     NOT NULL,           -- 主键ID
    tenant_id               VARCHAR(64)     NOT NULL DEFAULT 'default', -- 租户ID
    process_instance_id     VARCHAR(64)     NOT NULL,           -- 流程实例ID
    scope_execution_id      VARCHAR(64)     NOT NULL,           -- 作用域执行实例ID
    gateway_id              VARCHAR(128)    NOT NULL,           -- 网关节点ID
    cycle_key               VARCHAR(128)    NOT NULL,           -- 循环KEY（区分多次汇合）
    expected_count          INT             NOT NULL,           -- 期望汇合数
    arrived_count           INT             NOT NULL DEFAULT 0, -- 已到达数
    status                  VARCHAR(32)     NOT NULL DEFAULT 'WAITING', -- 状态（WAITING/COMPLETED）
    revision                BIGINT          NOT NULL DEFAULT 1, -- 乐观锁版本号
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时间
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='并行网关汇合状态表';

-- 唯一索引：同一网关同一循环唯一
CREATE UNIQUE INDEX uk_join_state ON wcdk_join_state(process_instance_id, scope_execution_id, gateway_id, cycle_key);
-- 索引：按流程实例ID查询
CREATE INDEX idx_join_process_instance ON wcdk_join_state(process_instance_id);

-- -------------------------------------------
-- 12. 事件外发表（Outbox Pattern）
-- 存储需要异步发送的事件
-- -------------------------------------------
CREATE TABLE wcdk_outbox (
    id                      VARCHAR(64)     NOT NULL,           -- 主键ID
    tenant_id               VARCHAR(64)     NOT NULL DEFAULT 'default', -- 租户ID
    aggregate_type          VARCHAR(64)     NOT NULL,           -- 聚合类型（PROCESS/TASK/JOB）
    aggregate_id            VARCHAR(64)     NOT NULL,           -- 聚合ID
    event_type              VARCHAR(64)     NOT NULL,           -- 事件类型
    payload                 TEXT            NOT NULL,           -- 事件负载（JSON）
    status                  VARCHAR(32)     NOT NULL DEFAULT 'PENDING', -- 状态（PENDING/PROCESSED/FAILED）
    retry_count             INT             NOT NULL DEFAULT 0, -- 重试次数
    max_retries             INT             NOT NULL DEFAULT 3, -- 最大重试次数
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    processed_at            TIMESTAMP       NULL,               -- 处理时间
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事件外发表';

-- 索引：按状态查询
CREATE INDEX idx_outbox_status ON wcdk_outbox(status);
-- 索引：按聚合查询
CREATE INDEX idx_outbox_aggregate ON wcdk_outbox(aggregate_type, aggregate_id);
-- 索引：按创建时间查询
CREATE INDEX idx_outbox_created_at ON wcdk_outbox(created_at);

-- -------------------------------------------
-- 13. 历史事件表（Append-Only）
-- 存储流程运行的历史事件（只增不改）
-- -------------------------------------------
CREATE TABLE wcdk_history_event (
    id                      VARCHAR(64)     NOT NULL,           -- 主键ID
    tenant_id               VARCHAR(64)     NOT NULL DEFAULT 'default', -- 租户ID
    process_instance_id     VARCHAR(64),                        -- 流程实例ID
    process_definition_id   VARCHAR(64),                        -- 流程定义ID
    execution_id            VARCHAR(64),                        -- 执行实例ID
    task_id                 VARCHAR(64),                        -- 任务ID
    job_id                  VARCHAR(64),                        -- 定时任务ID
    event_type              VARCHAR(64)     NOT NULL,           -- 事件类型（PROCESS_STARTED/PROCESS_COMPLETED/ACTIVITY_STARTED/ACTIVITY_COMPLETED/TASK_CREATED/TASK_CLAIMED/TASK_COMPLETED/VARIABLE_CREATED/VARIABLE_UPDATED/JOB_CREATED/JOB_COMPLETED/JOB_FAILED）
    event_name              VARCHAR(128),                       -- 事件名称
    node_id                 VARCHAR(128),                       -- 节点ID
    user_id                 VARCHAR(128),                       -- 用户ID
    payload                 TEXT,                               -- 事件负载（JSON）
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='历史事件表';

-- 索引：按流程实例ID查询
CREATE INDEX idx_he_process_instance ON wcdk_history_event(process_instance_id);
-- 索引：按执行实例ID查询
CREATE INDEX idx_he_execution ON wcdk_history_event(execution_id);
-- 索引：按任务ID查询
CREATE INDEX idx_he_task ON wcdk_history_event(task_id);
-- 索引：按事件类型查询
CREATE INDEX idx_he_event_type ON wcdk_history_event(event_type);
-- 索引：按创建时间查询
CREATE INDEX idx_he_created_at ON wcdk_history_event(created_at);

-- -------------------------------------------
-- 14. 分布式锁表
-- 存储分布式锁信息，支持多节点部署
-- -------------------------------------------
CREATE TABLE wcdk_distributed_lock (
    id                      VARCHAR(64)     NOT NULL,           -- 主键ID
    lock_key                VARCHAR(256)    NOT NULL,           -- 锁键
    token                   VARCHAR(128)    NOT NULL,           -- 锁令牌
    owner                   VARCHAR(128)    NOT NULL,           -- 锁持有者
    expires_at              TIMESTAMP       NOT NULL,           -- 锁过期时间
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    PRIMARY KEY (id),
    UNIQUE KEY uk_distributed_lock_key (lock_key),
    INDEX idx_distributed_lock_expires (expires_at),
    INDEX idx_distributed_lock_owner (owner)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分布式锁表';

-- -------------------------------------------
-- 15. Schema版本表
-- 存储数据库Schema版本信息
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS wcdk_schema_version (
    id                      BIGINT          NOT NULL AUTO_INCREMENT, -- 主键ID
    version                 VARCHAR(32)     NOT NULL,           -- 版本号
    description             VARCHAR(512),                       -- 描述
    applied_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 应用时间
    checksum                VARCHAR(64),                        -- 校验和
    PRIMARY KEY (id),
    INDEX idx_schema_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Schema版本表';

-- ============================================
-- 系统管理表
-- ============================================

-- -------------------------------------------
-- 16. 部门表
-- -------------------------------------------
CREATE TABLE SYS_DEPT (
    ID                      BIGINT          NOT NULL,           -- 主键
    PARENT_ID               BIGINT,                             -- 上级部门ID
    DEPT_CODE               VARCHAR(64)     NOT NULL,           -- 部门编码
    DEPT_NAME               VARCHAR(128)    NOT NULL,           -- 部门名称
    SORT_NO                 INT             DEFAULT 0,          -- 排序号
    STATUS                  INT             DEFAULT 1,          -- 状态
    REMARK                  VARCHAR(500),                       -- 备注
    CREATE_TIME             DATETIME(6),                        -- 创建时间
    UPDATE_TIME             DATETIME(6),                        -- 更新时间
    PRIMARY KEY (ID),
    UNIQUE KEY UK_SYS_DEPT_CODE (DEPT_CODE)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- -------------------------------------------
-- 17. 权限表
-- -------------------------------------------
CREATE TABLE SYS_PERMISSION (
    ID                      BIGINT          NOT NULL,           -- 主键
    PARENT_ID               BIGINT,                             -- 上级权限ID
    PERMISSION_CODE         VARCHAR(128)    NOT NULL,           -- 权限编码
    PERMISSION_NAME         VARCHAR(128)    NOT NULL,           -- 权限名称
    PERMISSION_TYPE         VARCHAR(32),                        -- 权限类型
    ROUTE_PATH              VARCHAR(255),                       -- 路由地址
    SORT_NO                 INT             DEFAULT 0,          -- 排序号
    STATUS                  INT             DEFAULT 1,          -- 状态
    REMARK                  VARCHAR(500),                       -- 备注
    CREATE_TIME             DATETIME(6),                        -- 创建时间
    UPDATE_TIME             DATETIME(6),                        -- 更新时间
    ICON                    VARCHAR(128),                       -- 图标
    PRIMARY KEY (ID),
    UNIQUE KEY UK_SYS_PERMISSION_CODE (PERMISSION_CODE)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- -------------------------------------------
-- 18. 角色表
-- -------------------------------------------
CREATE TABLE SYS_ROLE (
    ID                      BIGINT          NOT NULL,           -- 主键
    ROLE_CODE               VARCHAR(64)     NOT NULL,           -- 角色编码
    ROLE_NAME               VARCHAR(64)     NOT NULL,           -- 角色名称
    SORT_NO                 INT             DEFAULT 0,          -- 排序号
    STATUS                  INT             DEFAULT 1,          -- 状态
    REMARK                  VARCHAR(500),                       -- 备注
    CREATE_TIME             DATETIME(6),                        -- 创建时间
    UPDATE_TIME             DATETIME(6),                        -- 更新时间
    PRIMARY KEY (ID),
    UNIQUE KEY UK_SYS_ROLE_CODE (ROLE_CODE)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- -------------------------------------------
-- 19. 用户表
-- -------------------------------------------
CREATE TABLE SYS_USER (
    ID                      BIGINT          NOT NULL,           -- 主键
    DEPT_ID                 BIGINT,                             -- 所属部门ID
    USERNAME                VARCHAR(64)     NOT NULL,           -- 用户名
    PASSWORD_HASH           VARCHAR(64)     NOT NULL,           -- 密码摘要
    REAL_NAME               VARCHAR(64)     NOT NULL,           -- 姓名
    MOBILE                  VARCHAR(32),                        -- 手机号
    EMAIL                   VARCHAR(128),                       -- 邮箱
    STATUS                  INT             DEFAULT 1,          -- 状态
    LAST_LOGIN_TIME         DATETIME(6),                        -- 最后登录时间
    CREATE_TIME             DATETIME(6),                        -- 创建时间
    UPDATE_TIME             DATETIME(6),                        -- 更新时间
    PRIMARY KEY (ID),
    UNIQUE KEY UK_SYS_USER_USERNAME (USERNAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -------------------------------------------
-- 20. 角色权限关联表
-- -------------------------------------------
CREATE TABLE SYS_ROLE_PERMISSION (
    ROLE_ID                 BIGINT          NOT NULL,           -- 角色ID
    PERMISSION_ID           BIGINT          NOT NULL,           -- 权限ID
    CREATE_TIME             DATETIME(6),                        -- 创建时间
    PRIMARY KEY (ROLE_ID, PERMISSION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- -------------------------------------------
-- 21. 用户角色关联表
-- -------------------------------------------
CREATE TABLE SYS_USER_ROLE (
    USER_ID                 BIGINT          NOT NULL,           -- 用户ID
    ROLE_ID                 BIGINT          NOT NULL,           -- 角色ID
    CREATE_TIME             DATETIME(6),                        -- 创建时间
    PRIMARY KEY (USER_ID, ROLE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ============================================
-- WCDK流程引擎扩展表
-- ============================================

-- -------------------------------------------
-- 22. 客户端回调绑定表
-- -------------------------------------------
CREATE TABLE WCDK_PROCESS_CLIENT (
    ID                      BIGINT          NOT NULL,           -- 主键
    CLIENT_ID               VARCHAR(64)     NOT NULL,           -- 客户端标识
    CLIENT_NAME             VARCHAR(128)    NOT NULL,           -- 客户端名称
    CALLBACK_URL            VARCHAR(500),                       -- 回调地址
    AUTH_FLG                TEXT,                               -- 身份标识
    SERVICE_NAME            VARCHAR(255),                       -- 服务名
    CREATE_TIME             DATETIME(6),                        -- 创建时间
    UPDATE_TIME             DATETIME(6),                        -- 更新时间
    PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户端回调绑定表';

-- -------------------------------------------
-- 23. 客户端流程绑定表
-- -------------------------------------------
CREATE TABLE WCDK_PROCESS_CLIENT_PROCESS (
    ID                      BIGINT          NOT NULL,           -- 主键
    CLIENT_ID               VARCHAR(64)     NOT NULL,           -- 客户端标识
    PROCESS_BEAN_NAME       VARCHAR(128),                       -- 流程处理器名称
    PROCESS_DEFINITION_ID   VARCHAR(128),                       -- 流程定义ID
    PROCESS_NAME            VARCHAR(255),                       -- 流程名称
    EXCUTE_PARAM            VARCHAR(1000),                      -- 执行参数
    CREATE_TIME             DATETIME(6),                        -- 创建时间
    PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户端流程绑定表';

-- -------------------------------------------
-- 24. 流程定义元数据表
-- -------------------------------------------
CREATE TABLE WCDK_PROCESS_DEFINITION_META (
    ID                      BIGINT          NOT NULL,           -- 主键
    PROCESS_DEFINITION_ID   VARCHAR(128)    NOT NULL,           -- Flowable流程定义ID
    PROCESS_DEFINITION_KEY  VARCHAR(128),                       -- Flowable流程定义KEY
    PROCESS_DEFINITION_VERSION INT,                             -- Flowable流程定义版本
    DEPLOYMENT_ID           VARCHAR(128),                       -- Flowable部署ID
    INVALID_STATUS          INT             NOT NULL DEFAULT 0, -- 作废状态：0生效，1已作废
    CREATE_TIME             DATETIME(6),                        -- 创建时间
    UPDATE_TIME             DATETIME(6),                        -- 更新时间
    PRIMARY KEY (ID),
    UNIQUE KEY UK_WCDK_PROCESS_DEFINITION_META_DEF (PROCESS_DEFINITION_ID),
    INDEX IDX_WCDK_PROCESS_DEFINITION_META_KEY (PROCESS_DEFINITION_KEY),
    INDEX IDX_WCDK_PROCESS_DEFINITION_META_DEPLOY (DEPLOYMENT_ID),
    INDEX IDX_WCDK_PROCESS_DEFINITION_META_INVALID (INVALID_STATUS)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程定义元数据表';

-- -------------------------------------------
-- 25. 流程表单定义表
-- -------------------------------------------
CREATE TABLE WCDK_PROCESS_FORM (
    ID                      BIGINT          NOT NULL,           -- 主键
    FORM_KEY                VARCHAR(128)    NOT NULL,           -- 表单标识
    FORM_NAME               VARCHAR(255)    NOT NULL,           -- 表单名称
    FORM_VERSION            INT             NOT NULL DEFAULT 1, -- 表单版本
    FORM_SCHEMA_JSON        TEXT            NOT NULL,           -- 表单设计JSON
    RESOURCE_NAME           VARCHAR(255),                       -- 表单资源名称
    TENANT_ID               VARCHAR(128)    NOT NULL DEFAULT '', -- 租户ID
    STATUS                  INT             NOT NULL DEFAULT 1, -- 状态：1启用，0停用
    REMARK                  VARCHAR(500),                       -- 备注
    CREATE_USER             VARCHAR(64),                        -- 创建人
    CREATE_TIME             DATETIME(6),                        -- 创建时间
    UPDATE_TIME             DATETIME(6),                        -- 更新时间
    PRIMARY KEY (ID),
    UNIQUE KEY UK_WCDK_PROCESS_FORM_KEY_VER (FORM_KEY, FORM_VERSION, TENANT_ID),
    INDEX IDX_WCDK_PROCESS_FORM_KEY (FORM_KEY),
    INDEX IDX_WCDK_PROCESS_FORM_STATUS (STATUS)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程表单定义表';

-- -------------------------------------------
-- 26. 流程表单绑定表
-- -------------------------------------------
CREATE TABLE WCDK_PROCESS_FORM_BINDING (
    ID                      BIGINT          NOT NULL,           -- 主键
    FORM_ID                 BIGINT          NOT NULL,           -- 表单ID
    PROCESS_DEFINITION_ID   VARCHAR(128),                       -- Flowable流程定义ID
    PROCESS_DEFINITION_KEY  VARCHAR(128)    NOT NULL,           -- Flowable流程定义KEY
    PROCESS_DEFINITION_VERSION INT,                             -- Flowable流程定义版本
    DEPLOYMENT_ID           VARCHAR(128),                       -- Flowable部署ID
    TENANT_ID               VARCHAR(128)    NOT NULL DEFAULT '', -- 租户ID
    BIND_SCOPE              VARCHAR(32)     NOT NULL DEFAULT 'PROCESS', -- 绑定范围：PROCESS流程，START发起，TASK任务
    TASK_DEFINITION_KEY     VARCHAR(128)    NOT NULL DEFAULT '', -- 任务定义KEY
    STATUS                  INT             NOT NULL DEFAULT 1, -- 状态：1启用，0停用
    REMARK                  VARCHAR(500),                       -- 备注
    CREATE_TIME             DATETIME(6),                        -- 创建时间
    UPDATE_TIME             DATETIME(6),                        -- 更新时间
    PRIMARY KEY (ID),
    UNIQUE KEY UK_WCDK_PROCESS_FORM_BINDING_DEF (PROCESS_DEFINITION_ID, BIND_SCOPE, TASK_DEFINITION_KEY, TENANT_ID),
    INDEX IDX_WCDK_PROCESS_FORM_BINDING_FORM (FORM_ID),
    INDEX IDX_WCDK_PROCESS_FORM_BINDING_DEF_KEY (PROCESS_DEFINITION_KEY),
    INDEX IDX_WCDK_PROCESS_FORM_BINDING_DEPLOY (DEPLOYMENT_ID),
    INDEX IDX_WCDK_PROCESS_FORM_BINDING_DEF_ID (PROCESS_DEFINITION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程表单绑定表';

-- -------------------------------------------
-- 27. 流程申请业务表
-- -------------------------------------------
CREATE TABLE WCDK_PROCESS_REQUEST (
    ID                      BIGINT          NOT NULL,           -- 主键
    PROCESS_NO              VARCHAR(64)     NOT NULL,           -- 流程申请编号
    STARTER                 VARCHAR(64)     NOT NULL,           -- 流程发起人
    TASK_NAME               VARCHAR(255)    NOT NULL,           -- 任务名称
    BUSINESS_TITLE          VARCHAR(1000)   NOT NULL,           -- 业务标题
    FORM_DATA_JSON          TEXT,                               -- 动态表单数据JSON
    STATUS                  VARCHAR(32)     NOT NULL,           -- 流程申请状态
    PROCESS_INSTANCE_ID     VARCHAR(64),                        -- 流程实例ID
    CURRENT_TASK_ID         VARCHAR(64),                        -- 当前任务ID
    CURRENT_TASK_NAME       VARCHAR(128),                       -- 当前任务名称
    PROCESS_DEFINITION_KEY  VARCHAR(128)    NOT NULL,           -- 流程定义标识
    CREATE_TIME             DATETIME(6),                        -- 创建时间
    UPDATE_TIME             DATETIME(6),                        -- 更新时间
    PRIMARY KEY (ID),
    UNIQUE KEY UK_WCDK_PROCESS_REQUEST_NO (PROCESS_NO),
    INDEX IDX_WCDK_PROCESS_REQUEST_DEF_KEY (PROCESS_DEFINITION_KEY),
    INDEX IDX_WCDK_PROCESS_REQUEST_PROC (PROCESS_INSTANCE_ID),
    INDEX IDX_WCDK_PROCESS_REQUEST_STATUS (STATUS)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程申请业务表';

-- ============================================
-- 初始数据
-- ============================================

-- 初始化部门数据
INSERT INTO SYS_DEPT VALUES (1001, NULL, 'HEAD', '总部', 1, 1, '默认根部门', NOW(6), NOW(6));

-- 初始化权限数据
INSERT INTO SYS_PERMISSION VALUES (1101, NULL, 'menu:home', '首页', 'MENU', '/home', 1, 1, '首页菜单权限', NOW(6), NOW(6), 'House');
INSERT INTO SYS_PERMISSION VALUES (1102, NULL, 'menu:deploy', '部署中心', 'MENU', '/deploy', 2, 1, '部署中心菜单权限', NOW(6), NOW(6), 'Upload');
INSERT INTO SYS_PERMISSION VALUES (1103, NULL, 'menu:model', '模型中心', 'MENU', '/model', 3, 1, '模型中心菜单权限', NOW(6), NOW(6), 'Collection');
INSERT INTO SYS_PERMISSION VALUES (1104, NULL, 'menu:designer', '流程设计', 'MENU', '/designer', 4, 1, '流程设计菜单权限', NOW(6), NOW(6), 'Edit');
INSERT INTO SYS_PERMISSION VALUES (1105, NULL, 'menu:process', '流程中心', 'MENU', '/process', 5, 1, '流程中心菜单权限', NOW(6), NOW(6), 'Operation');
INSERT INTO SYS_PERMISSION VALUES (1106, NULL, 'menu:task', '任务中心', 'MENU', '/task', 6, 1, '任务中心菜单权限', NOW(6), NOW(6), 'CircleCheck');
INSERT INTO SYS_PERMISSION VALUES (1107, NULL, 'menu:sys:user', '用户管理', 'MENU', '/system/user', 7, 1, '用户管理菜单权限', NOW(6), NOW(6), 'User');
INSERT INTO SYS_PERMISSION VALUES (1108, NULL, 'menu:sys:role', '角色管理', 'MENU', '/system/role', 8, 1, '角色管理菜单权限', NOW(6), NOW(6), 'Avatar');
INSERT INTO SYS_PERMISSION VALUES (1109, NULL, 'menu:sys:permission', '权限管理', 'MENU', '/system/permission', 9, 1, '权限管理菜单权限', NOW(6), NOW(6), 'Key');
INSERT INTO SYS_PERMISSION VALUES (1110, NULL, 'menu:sys:dept', '部门管理', 'MENU', '/system/dept', 10, 1, '部门管理菜单权限', NOW(6), NOW(6), 'OfficeBuilding');
INSERT INTO SYS_PERMISSION VALUES (1111, 1107, 'sys:user:view', '查看用户', 'BUTTON', NULL, 11, 1, '用户查询权限', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1112, 1107, 'sys:user:add', '新增用户', 'BUTTON', NULL, 12, 1, '用户新增权限', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1113, 1107, 'sys:user:edit', '修改用户', 'BUTTON', NULL, 13, 1, '用户修改权限', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1114, 1107, 'sys:user:delete', '删除用户', 'BUTTON', NULL, 14, 1, '用户删除权限', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1115, 1108, 'sys:role:view', '查看角色', 'BUTTON', NULL, 15, 1, '角色查询权限', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1116, 1108, 'sys:role:add', '新增角色', 'BUTTON', NULL, 16, 1, '角色新增权限', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1117, 1108, 'sys:role:edit', '修改角色', 'BUTTON', NULL, 17, 1, '角色修改权限', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1118, 1108, 'sys:role:delete', '删除角色', 'BUTTON', NULL, 18, 1, '角色删除权限', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1119, 1109, 'sys:permission:view', '查看权限', 'BUTTON', NULL, 19, 1, '权限查询权限', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1120, 1109, 'sys:permission:add', '新增权限', 'BUTTON', NULL, 20, 1, '权限新增权限', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1121, 1109, 'sys:permission:edit', '修改权限', 'BUTTON', NULL, 21, 1, '权限修改权限', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1122, 1109, 'sys:permission:delete', '删除权限', 'BUTTON', NULL, 22, 1, '权限删除权限', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1123, 1110, 'sys:dept:view', '查看部门', 'BUTTON', NULL, 23, 1, '部门查询权限', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1124, 1110, 'sys:dept:add', '新增部门', 'BUTTON', NULL, 24, 1, '部门新增权限', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1125, 1110, 'sys:dept:edit', '修改部门', 'BUTTON', NULL, 25, 1, '部门修改权限', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1126, 1110, 'sys:dept:delete', '删除部门', 'BUTTON', NULL, 26, 1, '部门删除权限', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1127, NULL, '*:*:*', '系统全量权限', 'BUTTON', NULL, 99, 1, '管理员全量权限', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1128, NULL, 'menu:form', '表单设计', 'MENU', '/form-designer', 5, 1, '表单设计菜单权限', NOW(6), NOW(6), 'Tickets');
INSERT INTO SYS_PERMISSION VALUES (1130, 1102, 'deploy:tab:create', '创建部署', 'TAB', NULL, 1, 1, '部署中心创建部署', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1131, 1102, 'deploy:tab:list', '流程列表', 'TAB', NULL, 2, 1, '部署中心流程列表', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1132, 1103, 'model:tab:create', '创建模型', 'TAB', NULL, 1, 1, '模型中心创建模型', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1133, 1103, 'model:tab:list', '模型列表', 'TAB', NULL, 2, 1, '模型中心模型列表', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1134, 1105, 'process:tab:create', '创建流程', 'TAB', NULL, 1, 1, '流程中心创建流程', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1135, 1105, 'process:tab:list', '流程列表', 'TAB', NULL, 2, 1, '流程中心流程列表', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1136, 1102, 'deploy:refresh', '刷新部署', 'BUTTON', NULL, 10, 1, '刷新部署中心数据', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1137, 1102, 'deploy:create', '提交部署', 'BUTTON', NULL, 11, 1, '提交流程部署', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1138, 1102, 'deploy:start', '发起审批', 'BUTTON', NULL, 12, 1, '从部署发起审批', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1139, 1102, 'deploy:edit', '编辑部署', 'BUTTON', NULL, 13, 1, '编辑部署流程定义', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1140, 1102, 'deploy:view', '查看部署', 'BUTTON', NULL, 14, 1, '查看部署详情', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1141, 1102, 'deploy:preview', '预览部署', 'BUTTON', NULL, 15, 1, '预览部署流程图', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1142, 1102, 'deploy:binding', '修改部署绑定', 'BUTTON', NULL, 16, 1, '修改部署客户端绑定', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1143, 1102, 'deploy:delete', '删除部署', 'BUTTON', NULL, 17, 1, '删除部署数据', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1144, 1103, 'model:refresh', '刷新模型', 'BUTTON', NULL, 10, 1, '刷新模型中心数据', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1145, 1103, 'model:create', '创建模型', 'BUTTON', NULL, 11, 1, '创建流程模型', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1146, 1103, 'model:edit', '修改模型', 'BUTTON', NULL, 12, 1, '修改流程模型', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1147, 1103, 'model:preview', '预览模型', 'BUTTON', NULL, 13, 1, '预览流程模型', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1148, 1103, 'model:deploy', '部署模型', 'BUTTON', NULL, 14, 1, '部署流程模型', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1149, 1103, 'model:delete', '删除模型', 'BUTTON', NULL, 15, 1, '删除流程模型', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1150, 1105, 'process:refresh', '刷新流程', 'BUTTON', NULL, 10, 1, '刷新流程中心数据', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1151, 1105, 'process:view', '查看流程', 'BUTTON', NULL, 11, 1, '查看流程详情', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1152, 1105, 'process:submit', '提交流程', 'BUTTON', NULL, 12, 1, '提交草稿流程', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1153, 1105, 'process:delete', '删除流程', 'BUTTON', NULL, 13, 1, '删除流程申请', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1154, 1105, 'process:action:draft', '保存草稿', 'BUTTON', NULL, 14, 1, '流程表单保存草稿按钮', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1155, 1105, 'process:action:submit', '提交申请', 'BUTTON', NULL, 15, 1, '流程表单提交申请按钮', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1156, 1105, 'process:action:reset', '重置流程表单', 'BUTTON', NULL, 16, 1, '流程表单重置按钮', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1157, 1106, 'task:refresh', '刷新任务', 'BUTTON', NULL, 10, 1, '刷新任务中心数据', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1158, 1106, 'task:view', '查看任务', 'BUTTON', NULL, 11, 1, '查看任务流程图', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1159, 1106, 'task:approve', '办理任务', 'BUTTON', NULL, 12, 1, '办理审批任务', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1160, 1106, 'task:delete', '删除任务', 'BUTTON', NULL, 13, 1, '删除任务', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1163, 1105, 'process:status:tag', '流程状态', 'TAG', NULL, 30, 1, '流程中心状态资源', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1164, 1104, 'designer:export:bpmn', '导出 BPMN', 'BUTTON', NULL, 10, 1, '导出 BPMN 文件', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1165, 1104, 'designer:export:bpmn-xml', '导出 BPMN.XML', 'BUTTON', NULL, 11, 1, '导出 BPMN.XML 文件', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1166, 1104, 'designer:export:png', '导出 PNG', 'BUTTON', NULL, 12, 1, '导出 PNG 图片', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1167, 1104, 'designer:canvas:center', '居中显示', 'BUTTON', NULL, 13, 1, '将画布内容居中显示', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1168, 1104, 'designer:canvas:reset', '清空画布', 'BUTTON', NULL, 14, 1, '清空画布内容', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1169, 1104, 'designer:canvas:refresh', '刷新画布', 'BUTTON', NULL, 15, 1, '刷新画布内容', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1170, 1128, 'form:save', '保存表单方案', 'BUTTON', NULL, 10, 1, '保存表单设计方案', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1171, 1128, 'form:load', '载入表单方案', 'BUTTON', NULL, 11, 1, '载入表单设计方案', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1172, 1128, 'form:export', '导出表单JSON', 'BUTTON', NULL, 12, 1, '导出表单设计JSON', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1173, 1128, 'form:import', '导入表单JSON', 'BUTTON', NULL, 13, 1, '导入表单设计JSON', NOW(6), NOW(6), NULL);
INSERT INTO SYS_PERMISSION VALUES (1174, 1128, 'form:jump:designer', '跳转流程设计', 'BUTTON', NULL, 14, 1, '从表单设计跳转流程设计', NOW(6), NOW(6), NULL);

-- 初始化角色数据
INSERT INTO SYS_ROLE VALUES (1001, 'ADMIN', '系统管理员', 1, 1, '默认管理员角色', NOW(6), NOW(6));

-- 初始化用户数据（默认密码: admin123）
INSERT INTO SYS_USER VALUES (1001, 1001, 'admin', '0192023A7BBD73250516F069DF18B500', '系统管理员', NULL, NULL, 1, NULL, NOW(6), NOW(6));

-- 初始化用户角色关联数据
INSERT INTO SYS_USER_ROLE VALUES (1001, 1001, NOW(6));

-- 初始化角色权限关联数据（管理员拥有所有权限）
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1101, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1102, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1130, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1131, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1136, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1137, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1138, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1139, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1140, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1141, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1142, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1143, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1103, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1132, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1133, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1144, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1145, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1146, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1147, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1148, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1149, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1104, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1164, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1165, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1166, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1167, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1168, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1169, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1105, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1134, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1135, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1150, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1151, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1152, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1153, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1154, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1155, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1156, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1163, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1106, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1157, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1158, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1159, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1160, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1107, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1111, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1112, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1113, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1114, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1108, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1115, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1116, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1117, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1118, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1109, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1119, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1120, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1121, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1122, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1110, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1123, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1124, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1125, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1126, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1127, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1170, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1171, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1172, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1173, NOW(6));
INSERT INTO SYS_ROLE_PERMISSION VALUES (1001, 1174, NOW(6));
