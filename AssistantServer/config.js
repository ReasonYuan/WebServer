//开发环境
var development = {
  "urls": {
    "couchbase_cluster":"couchbase://localhost/",
    "sync_gateway":"http://localhost:4985/",
    "sync_gateway_urls": {
      "get_session":"/_session"
    },
  },
  "buckets": {
    "couchbase_server":"test"
  },
  "apiKeys": {
    "yunpian":"39ba44d0b8fa8e22a02ec47b7d60e02d",
    "pingDoublePlus": "sk_test_5eLGeL9OGej95KuXD4ffrz1O"
  },
  templates: {
    SMSTemplates: {
      company_code: {
        templateId: 2,
        templateValue: "#code#={0}&#company#=翼依信息技术"
      }
    }
  }
}
//bug重现环境
var bugreproduce = {
  "urls": {
    "couchbase_cluster":"couchbase://localhost/",
    "sync_gateway":"http://localhost:4985/",
    "sync_gateway_urls": {
      "get_session":"/_session"
    },
  },
  "buckets": {
    "couchbase_server":"test"
  },
  "apiKeys": {
    "yunpian":"39ba44d0b8fa8e22a02ec47b7d60e02d",
    "pingDoublePlus": "sk_test_5eLGeL9OGej95KuXD4ffrz1O"
  },
  templates: {
    SMSTemplates: {
      company_code: {
        templateId: 2,
        templateValue: "#code#={0}&#company#=翼依信息技术"
      }
    }
  }
}
//demo环境
var demo = {
  "urls": {
    "couchbase_cluster":"couchbase://localhost/",
    "sync_gateway":"http://localhost:4985/",
    "sync_gateway_urls": {
      "get_session":"/_session"
    },
  },
  "buckets": {
    "couchbase_server":"demo"
  },
  "apiKeys": {
    "yunpian":"39ba44d0b8fa8e22a02ec47b7d60e02d",
    "pingDoublePlus": "sk_test_5eLGeL9OGej95KuXD4ffrz1O"
  },
  templates: {
    SMSTemplates: {
      company_code: {
        templateId: 2,
        templateValue: "#code#={0}&#company#=翼依信息技术"
      }
    }
  }
}
//生产环境
var production = {
  "urls": {
    "couchbase_cluster":"couchbase://10.47.125.58/",
    "sync_gateway":"http://10.47.127.152:4985/",
    "sync_gateway_urls": {
      "get_session":"/_session"
    },
  },
  "buckets": {
    "couchbase_server":"production"
  },
  "apiKeys": {
    "yunpian":"39ba44d0b8fa8e22a02ec47b7d60e02d",
    "pingDoublePlus": "sk_test_5eLGeL9OGej95KuXD4ffrz1O"
  },
  templates: {
    SMSTemplates: {
      company_code: {
        templateId: 2,
        templateValue: "#code#={0}&#company#=翼依信息技术"
      }
    }
  }
}
//测试环境
var test = {
  "urls": {
    "couchbase_cluster":"couchbase://10.46.69.222/",
    "sync_gateway":"http://10.47.127.139:4985/",
    "sync_gateway_urls": {
      "get_session":"/_session"
    },
  },
  "buckets": {
    "couchbase_server":"test"
  },
  "apiKeys": {
    "yunpian":"39ba44d0b8fa8e22a02ec47b7d60e02d",
    "pingDoublePlus": "sk_test_5eLGeL9OGej95KuXD4ffrz1O"
  },
  templates: {
    SMSTemplates: {
      company_code: {
        templateId: 2,
        templateValue: "#code#={0}&#company#=翼依信息技术"
      }
    }
  }
}
//预生产环境
var preproduction = {
  "urls": {
    "couchbase_cluster":"couchbase://10.45.51.91/",
    "sync_gateway":"http://10.45.51.95:4985/",
    "sync_gateway_urls": {
      "get_session":"/_session"
    },
  },
  "buckets": {
    "couchbase_server":"production"
  },
  "apiKeys": {
    "yunpian":"39ba44d0b8fa8e22a02ec47b7d60e02d",
    "pingDoublePlus": "sk_test_5eLGeL9OGej95KuXD4ffrz1O"
  },
  templates: {
    SMSTemplates: {
      company_code: {
        templateId: 2,
        templateValue: "#code#={0}&#company#=翼依信息技术"
      }
    }
  }
}

switch(process.env.NODE_ENV){
  case 'production':
        module.exports = production
        break;
  case 'demo':
        module.exports = demo
        break;
  case 'bugreproduce':
        module.exports = bugreproduce
        break;
  case 'test':
        module.exports = test
        break;
  case 'preproduction':
        module.exports = preproduction
        break;
  case 'development':
        module.exports = development
        break;
  default :
        console.log("Error, can not find NODE_ENV, is not set!");
        break;
}

