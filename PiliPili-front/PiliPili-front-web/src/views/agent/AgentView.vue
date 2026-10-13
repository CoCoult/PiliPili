<template>
  <div class="agent-container">
    <div class="agent-header">
      <div class="agent-title">
        <div class="agent-title-icon">AI</div>
        <div>
          <div class="agent-title-main">agent开发</div>
          <div class="agent-title-sub">后端开发 &amp; Agent 开发常用网站资源汇总</div>
        </div>
      </div>
      <div class="agent-search">
        <el-input
          v-model="keyword"
          placeholder="搜索资源名称 / 描述"
          clearable
          :prefix-icon="'Search'"
        ></el-input>
      </div>
    </div>

    <div class="agent-stats">
      <span>共 {{ totalCount }} 个资源</span>
      <span class="dot"></span>
      <span>{{ filteredGroups.length }} 个分类</span>
    </div>

    <div class="agent-group" v-for="group in filteredGroups" :key="group.title">
      <div class="group-head">
        <span class="group-icon">{{ group.icon }}</span>
        <span class="group-title">{{ group.title }}</span>
        <span class="group-count">{{ group.items.length }}</span>
      </div>
      <div class="resource-grid">
        <a
          class="resource-card"
          v-for="item in group.items"
          :key="item.url"
          :href="item.url"
          target="_blank"
          rel="noopener noreferrer"
        >
          <div class="resource-name">
            {{ item.name }}
            <span class="iconfont icon-more"></span>
          </div>
          <div class="resource-desc">{{ item.desc }}</div>
        </a>
      </div>
    </div>

    <el-empty
      v-if="filteredGroups.length == 0"
      description="没有匹配的资源"
    ></el-empty>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";

const keyword = ref("");

const resourceGroups = [
  {
    title: "后端开发 · Java 生态",
    icon: "☕",
    items: [
      { name: "Spring", url: "https://spring.io/", desc: "Spring 全家桶官方站，Boot / Cloud 文档入口" },
      { name: "Oracle Java", url: "https://docs.oracle.com/en/java/javase/index.html", desc: "Java 官方文档与 API 参考" },
      { name: "MyBatis", url: "https://mybatis.org/mybatis-3/zh_CN/index.html", desc: "MyBatis 中文文档" },
      { name: "Maven Central", url: "https://search.maven.org/", desc: "Maven 构件仓库检索" },
    ],
  },
  {
    title: "后端开发 · 数据库与缓存",
    icon: "🗄️",
    items: [
      { name: "MySQL", url: "https://dev.mysql.com/doc/", desc: "MySQL 官方文档" },
      { name: "Redis", url: "https://redis.io/", desc: "Redis 官网与文档" },
      { name: "Elasticsearch", url: "https://www.elastic.co/cn/elasticsearch/", desc: "Elasticsearch 官方文档" },
      { name: "MongoDB", url: "https://www.mongodb.com/", desc: "MongoDB 官方文档" },
    ],
  },
  {
    title: "后端开发 · 中间件与基础设施",
    icon: "⚙️",
    items: [
      { name: "Nginx", url: "https://nginx.org/", desc: "Nginx 反向代理 / 负载均衡" },
      { name: "Docker", url: "https://www.docker.com/", desc: "容器化部署官方站" },
      { name: "Kubernetes", url: "https://kubernetes.io/", desc: "K8s 容器编排文档" },
      { name: "Kafka", url: "https://kafka.apache.org/", desc: "分布式消息队列" },
      { name: "RabbitMQ", url: "https://www.rabbitmq.com/", desc: "消息队列官方文档" },
    ],
  },
  {
    title: "后端开发 · 语言与运行时",
    icon: "🔧",
    items: [
      { name: "Node.js", url: "https://nodejs.org/", desc: "Node.js 官网与文档" },
      { name: "Python", url: "https://www.python.org/", desc: "Python 官网" },
      { name: "Go", url: "https://go.dev/", desc: "Go 语言官网" },
      { name: "Rust", url: "https://www.rust-lang.org/", desc: "Rust 语言官网" },
    ],
  },
  {
    title: "后端开发 · 学习教程",
    icon: "📚",
    items: [
      { name: "菜鸟教程", url: "https://www.runoob.com/", desc: "各类语言与中间件入门教程" },
      { name: "MDN", url: "https://developer.mozilla.org/zh-CN/", desc: "Web 开发权威文档" },
      { name: "廖雪峰", url: "https://www.liaoxuefeng.com/", desc: "Java / Python / Git 教程" },
      { name: "W3Schools", url: "https://www.w3schools.com/", desc: "Web 技术入门教程" },
    ],
  },
  {
    title: "后端开发 · 社区问答",
    icon: "💬",
    items: [
      { name: "GitHub", url: "https://github.com/", desc: "开源代码托管与协作" },
      { name: "Stack Overflow", url: "https://stackoverflow.com/", desc: "全球程序员问答社区" },
      { name: "掘金", url: "https://juejin.cn/", desc: "中文开发者技术社区" },
      { name: "CSDN", url: "https://www.csdn.net/", desc: "中文 IT 技术社区" },
      { name: "SegmentFault", url: "https://segmentfault.com/", desc: "中文技术问答社区" },
      { name: "InfoQ 中文", url: "https://www.infoq.cn/", desc: "技术资讯与深度内容" },
      { name: "V2EX", url: "https://www.v2ex.com/", desc: "开发者创意社区" },
    ],
  },
  {
    title: "后端开发 · API 与调试工具",
    icon: "🧰",
    items: [
      { name: "Postman", url: "https://www.postman.com/", desc: "API 调试与测试工具" },
      { name: "Apifox", url: "https://apifox.com/", desc: "API 设计 / 调试 / 文档一体化" },
      { name: "Swagger", url: "https://swagger.io/", desc: "OpenAPI 规范与文档工具" },
      { name: "JSON 在线工具", url: "https://www.json.cn/", desc: "JSON 解析 / 格式化" },
      { name: "regex101", url: "https://regex101.com/", desc: "在线正则调试" },
      { name: "crontab.guru", url: "https://crontab.guru/", desc: "Cron 定时表达式工具" },
    ],
  },
  {
    title: "Agent 开发 · 大模型平台",
    icon: "🤖",
    items: [
      { name: "OpenAI Platform", url: "https://platform.openai.com/", desc: "GPT 系列 API 平台" },
      { name: "Anthropic", url: "https://www.anthropic.com/", desc: "Claude 系列模型" },
      { name: "DeepSeek", url: "https://www.deepseek.com/", desc: "DeepSeek 开放平台" },
      { name: "Google AI", url: "https://ai.google.dev/", desc: "Gemini 模型 API" },
      { name: "百度千帆", url: "https://qianfan.cloud.baidu.com/", desc: "百度大模型平台" },
      { name: "阿里云百炼", url: "https://bailian.console.aliyun.com/", desc: "通义系列模型平台" },
      { name: "讯飞星火", url: "https://xinghuo.xfyun.cn/", desc: "讯飞星火大模型" },
      { name: "智谱开放平台", url: "https://open.bigmodel.cn/", desc: "GLM 系列模型 API" },
      { name: "Kimi 开放平台", url: "https://platform.moonshot.cn/", desc: "Moonshot 系列模型 API" },
    ],
  },
  {
    title: "Agent 开发 · 框架与编排",
    icon: "🧩",
    items: [
      { name: "LangChain", url: "https://www.langchain.com/", desc: "最流行的 LLM 应用框架" },
      { name: "LlamaIndex", url: "https://www.llamaindex.ai/", desc: "RAG 数据框架" },
      { name: "LangGraph", url: "https://langchain-ai.github.io/langgraph/", desc: "有状态多智能体编排" },
      { name: "AutoGPT", url: "https://github.com/Significant-Gravitas/AutoGPT", desc: "自主智能体项目" },
      { name: "MetaGPT", url: "https://github.com/geekan/MetaGPT", desc: "多智能体协作框架" },
      { name: "CrewAI", url: "https://www.crewai.com/", desc: "角色化多智能体编排" },
      { name: "Dify", url: "https://dify.ai/", desc: "低代码 LLM 应用平台" },
      { name: "Coze", url: "https://www.coze.com/", desc: "字节扣子智能体平台" },
      { name: "Spring AI", url: "https://spring.io/projects/spring-ai", desc: "Java 生态 AI 开发框架" },
      { name: "Semantic Kernel", url: "https://github.com/microsoft/semantic-kernel", desc: "微软 AI 编排 SDK" },
    ],
  },
  {
    title: "Agent 开发 · MCP 生态",
    icon: "🔌",
    items: [
      { name: "Model Context Protocol", url: "https://modelcontextprotocol.io/", desc: "MCP 官方规范文档" },
      { name: "MCP Servers", url: "https://github.com/modelcontextprotocol/servers", desc: "官方 MCP Server 集合" },
    ],
  },
  {
    title: "Agent 开发 · 向量数据库与 RAG",
    icon: "📐",
    items: [
      { name: "Pinecone", url: "https://www.pinecone.io/", desc: "托管向量数据库" },
      { name: "Milvus", url: "https://milvus.io/", desc: "开源向量数据库" },
      { name: "Chroma", url: "https://www.trychroma.com/", desc: "轻量向量数据库" },
      { name: "Weaviate", url: "https://weaviate.io/", desc: "开源向量数据库" },
      { name: "Qdrant", url: "https://qdrant.tech/", desc: "高性能向量检索" },
      { name: "FAISS", url: "https://github.com/facebookresearch/faiss", desc: "Meta 向量检索库" },
    ],
  },
  {
    title: "Agent 开发 · 模型与评测",
    icon: "🔬",
    items: [
      { name: "Hugging Face", url: "https://huggingface.co/", desc: "模型 / 数据集 / 社区" },
      { name: "arXiv", url: "https://arxiv.org/", desc: "论文预印本" },
      { name: "Papers with Code", url: "https://paperswithcode.com/", desc: "论文与代码对照" },
      { name: "OpenAI Evals", url: "https://github.com/openai/evals", desc: "LLM 评测框架" },
      { name: "Prompt Engineering Guide", url: "https://www.promptingguide.ai/zh", desc: "提示工程指南" },
    ],
  },
];

const filteredGroups = computed(() => {
  const kw = keyword.value.trim().toLowerCase();
  if (!kw) {
    return resourceGroups;
  }
  return resourceGroups
    .map((group) => ({
      ...group,
      items: group.items.filter(
        (item) =>
          item.name.toLowerCase().includes(kw) ||
          item.desc.toLowerCase().includes(kw) ||
          item.url.toLowerCase().includes(kw)
      ),
    }))
    .filter((group) => group.items.length > 0);
});

const totalCount = computed(() =>
  resourceGroups.reduce((sum, group) => sum + group.items.length, 0)
);
</script>

<style lang="scss" scoped>
.agent-container {
  margin: 20px auto 40px;
  min-width: 1070px;
  max-width: 1280px;

  .agent-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #ddd;
    padding: 10px 0px 20px;

    .agent-title {
      display: flex;
      align-items: center;

      .agent-title-icon {
        width: 56px;
        height: 56px;
        background: linear-gradient(135deg, #00aeec, #007fff);
        color: #fff;
        font-size: 18px;
        font-weight: bold;
        border-radius: 14px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 14px;
      }

      .agent-title-main {
        font-size: 22px;
        font-weight: bold;
        color: var(--text);
      }

      .agent-title-sub {
        font-size: 13px;
        color: var(--text3);
        margin-top: 4px;
      }
    }

    .agent-search {
      width: 280px;
    }
  }

  .agent-stats {
    display: flex;
    align-items: center;
    color: var(--text3);
    font-size: 13px;
    padding: 12px 0px 4px;

    .dot {
      width: 3px;
      height: 3px;
      border-radius: 50%;
      background: var(--text3);
      margin: 0px 8px;
    }
  }

  .agent-group {
    margin-top: 24px;

    .group-head {
      display: flex;
      align-items: center;
      margin-bottom: 12px;

      .group-icon {
        font-size: 18px;
        margin-right: 8px;
      }

      .group-title {
        font-size: 17px;
        font-weight: bold;
        color: var(--text);
        margin-right: 8px;
      }

      .group-count {
        font-size: 12px;
        color: #fff;
        background: var(--blue);
        border-radius: 10px;
        padding: 1px 8px;
      }
    }

    .resource-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 12px;

      .resource-card {
        text-decoration: none;
        border: 1px solid #e3e5e7;
        border-radius: 8px;
        padding: 14px 16px;
        background: #fff;
        transition: all 0.2s;

        &:hover {
          border-color: var(--blue);
          box-shadow: 0 4px 12px rgba(0, 174, 236, 0.15);
          transform: translateY(-2px);
        }

        .resource-name {
          font-size: 15px;
          font-weight: 600;
          color: var(--text);
          display: flex;
          justify-content: space-between;
          align-items: center;

          .iconfont {
            font-size: 12px;
            color: var(--text3);
          }
        }

        .resource-desc {
          margin-top: 6px;
          font-size: 13px;
          color: var(--text3);
          line-height: 1.5;
        }
      }
    }
  }
}

@media screen and (max-width: 1500px) {
  .agent-container {
    max-width: 1200px;
  }
}
</style>
