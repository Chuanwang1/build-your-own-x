// MongoDB 初始化脚本
// 用于存储课程内容、代码模板、用户笔记等文档数据

// 切换到目标数据库
db = db.getSiblingDB('programming_platform_docs');

// 创建用户
db.createUser({
  user: 'app_user',
  pwd: '8888',
  roles: [
    {
      role: 'readWrite',
      db: 'programming_platform_docs'
    }
  ]
});

// 课程内容集合
db.createCollection('lesson_content', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['lessonId', 'courseId', 'contentType', 'content'],
      properties: {
        lessonId: {
          bsonType: 'long',
          description: '课时ID，必须是数字'
        },
        courseId: {
          bsonType: 'long',
          description: '课程ID，必须是数字'
        },
        contentType: {
          bsonType: 'string',
          enum: ['markdown', 'html', 'video', 'interactive'],
          description: '内容类型'
        },
        content: {
          bsonType: 'object',
          description: '课程内容数据'
        },
        metadata: {
          bsonType: 'object',
          description: '元数据信息'
        },
        version: {
          bsonType: 'int',
          minimum: 1,
          description: '内容版本号'
        },
        createdAt: {
          bsonType: 'date',
          description: '创建时间'
        },
        updatedAt: {
          bsonType: 'date',
          description: '更新时间'
        }
      }
    }
  }
});

// 创建索引
db.lesson_content.createIndex({ lessonId: 1 }, { unique: true });
db.lesson_content.createIndex({ courseId: 1 });
db.lesson_content.createIndex({ contentType: 1 });
db.lesson_content.createIndex({ 'metadata.tags': 1 });
db.lesson_content.createIndex({ createdAt: -1 });

// 代码模板集合
db.createCollection('code_templates', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['language', 'templateType', 'code'],
      properties: {
        language: {
          bsonType: 'string',
          enum: ['java', 'python', 'javascript', 'cpp', 'csharp', 'go', 'rust'],
          description: '编程语言'
        },
        templateType: {
          bsonType: 'string',
          enum: ['basic', 'class', 'function', 'algorithm', 'data-structure'],
          description: '模板类型'
        },
        name: {
          bsonType: 'string',
          description: '模板名称'
        },
        description: {
          bsonType: 'string',
          description: '模板描述'
        },
        code: {
          bsonType: 'string',
          description: '模板代码'
        },
        placeholders: {
          bsonType: 'array',
          items: {
            bsonType: 'object',
            properties: {
              name: { bsonType: 'string' },
              description: { bsonType: 'string' },
              defaultValue: { bsonType: 'string' }
            }
          },
          description: '代码占位符'
        },
        tags: {
          bsonType: 'array',
          items: { bsonType: 'string' },
          description: '标签'
        },
        difficulty: {
          bsonType: 'string',
          enum: ['beginner', 'intermediate', 'advanced'],
          description: '难度级别'
        },
        isPublic: {
          bsonType: 'bool',
          description: '是否公开'
        },
        createdBy: {
          bsonType: 'long',
          description: '创建者用户ID'
        },
        createdAt: {
          bsonType: 'date',
          description: '创建时间'
        },
        updatedAt: {
          bsonType: 'date',
          description: '更新时间'
        }
      }
    }
  }
});

// 创建索引
db.code_templates.createIndex({ language: 1, templateType: 1 });
db.code_templates.createIndex({ tags: 1 });
db.code_templates.createIndex({ difficulty: 1 });
db.code_templates.createIndex({ isPublic: 1 });
db.code_templates.createIndex({ createdBy: 1 });
db.code_templates.createIndex({ name: 'text', description: 'text' });

// 用户笔记集合
db.createCollection('user_notes', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['userId', 'noteType', 'content'],
      properties: {
        userId: {
          bsonType: 'long',
          description: '用户ID'
        },
        courseId: {
          bsonType: 'long',
          description: '课程ID'
        },
        lessonId: {
          bsonType: 'long',
          description: '课时ID'
        },
        noteType: {
          bsonType: 'string',
          enum: ['text', 'code', 'bookmark', 'question'],
          description: '笔记类型'
        },
        title: {
          bsonType: 'string',
          description: '笔记标题'
        },
        content: {
          bsonType: 'string',
          description: '笔记内容'
        },
        tags: {
          bsonType: 'array',
          items: { bsonType: 'string' },
          description: '标签'
        },
        isPrivate: {
          bsonType: 'bool',
          description: '是否私有'
        },
        position: {
          bsonType: 'object',
          properties: {
            timestamp: { bsonType: 'int' },
            line: { bsonType: 'int' },
            column: { bsonType: 'int' }
          },
          description: '笔记位置信息'
        },
        createdAt: {
          bsonType: 'date',
          description: '创建时间'
        },
        updatedAt: {
          bsonType: 'date',
          description: '更新时间'
        }
      }
    }
  }
});

// 创建索引
db.user_notes.createIndex({ userId: 1, courseId: 1 });
db.user_notes.createIndex({ userId: 1, lessonId: 1 });
db.user_notes.createIndex({ noteType: 1 });
db.user_notes.createIndex({ tags: 1 });
db.user_notes.createIndex({ createdAt: -1 });
db.user_notes.createIndex({ title: 'text', content: 'text' });

// 练习题集合
db.createCollection('exercises', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['lessonId', 'title', 'description', 'language', 'testCases'],
      properties: {
        lessonId: {
          bsonType: 'long',
          description: '课时ID'
        },
        title: {
          bsonType: 'string',
          description: '练习题标题'
        },
        description: {
          bsonType: 'string',
          description: '题目描述'
        },
        language: {
          bsonType: 'string',
          description: '编程语言'
        },
        difficulty: {
          bsonType: 'string',
          enum: ['easy', 'medium', 'hard'],
          description: '难度级别'
        },
        starterCode: {
          bsonType: 'string',
          description: '初始代码'
        },
        solutionCode: {
          bsonType: 'string',
          description: '参考答案'
        },
        testCases: {
          bsonType: 'array',
          items: {
            bsonType: 'object',
            properties: {
              input: { bsonType: 'string' },
              expectedOutput: { bsonType: 'string' },
              isHidden: { bsonType: 'bool' },
              description: { bsonType: 'string' }
            }
          },
          description: '测试用例'
        },
        hints: {
          bsonType: 'array',
          items: { bsonType: 'string' },
          description: '提示信息'
        },
        timeLimit: {
          bsonType: 'int',
          description: '时间限制（秒）'
        },
        memoryLimit: {
          bsonType: 'int',
          description: '内存限制（KB）'
        },
        tags: {
          bsonType: 'array',
          items: { bsonType: 'string' },
          description: '标签'
        },
        createdAt: {
          bsonType: 'date',
          description: '创建时间'
        },
        updatedAt: {
          bsonType: 'date',
          description: '更新时间'
        }
      }
    }
  }
});

// 创建索引
db.exercises.createIndex({ lessonId: 1 });
db.exercises.createIndex({ language: 1 });
db.exercises.createIndex({ difficulty: 1 });
db.exercises.createIndex({ tags: 1 });
db.exercises.createIndex({ title: 'text', description: 'text' });

// 插入示例数据

// 示例课程内容
db.lesson_content.insertMany([
  {
    lessonId: NumberLong(1),
    courseId: NumberLong(1),
    contentType: 'markdown',
    content: {
      markdown: `# Java 基础入门

## 什么是 Java？

Java 是一种面向对象的编程语言，具有以下特点：

- **跨平台性**：一次编写，到处运行
- **面向对象**：支持封装、继承、多态
- **安全性**：内置安全机制
- **多线程**：支持并发编程

## Hello World 程序

\`\`\`java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
\`\`\`

这是最简单的 Java 程序示例。`
    },
    metadata: {
      tags: ['java', 'basics', 'introduction'],
      estimatedReadTime: 5,
      difficulty: 'beginner'
    },
    version: 1,
    createdAt: new Date(),
    updatedAt: new Date()
  }
]);

// 示例代码模板
db.code_templates.insertMany([
  {
    language: 'java',
    templateType: 'basic',
    name: 'Java Main Class',
    description: 'Basic Java class with main method',
    code: `public class {{className}} {
    public static void main(String[] args) {
        // Your code here
        {{code}}
    }
}`,
    placeholders: [
      {
        name: 'className',
        description: 'Class name',
        defaultValue: 'Main'
      },
      {
        name: 'code',
        description: 'Main code',
        defaultValue: 'System.out.println("Hello, World!");'
      }
    ],
    tags: ['java', 'basic', 'main'],
    difficulty: 'beginner',
    isPublic: true,
    createdBy: NumberLong(1),
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    language: 'python',
    templateType: 'basic',
    name: 'Python Basic Script',
    description: 'Basic Python script template',
    code: `#!/usr/bin/env python3
# -*- coding: utf-8 -*-

def main():
    # Your code here
    {{code}}

if __name__ == "__main__":
    main()`,
    placeholders: [
      {
        name: 'code',
        description: 'Main code',
        defaultValue: 'print("Hello, World!")'
      }
    ],
    tags: ['python', 'basic', 'script'],
    difficulty: 'beginner',
    isPublic: true,
    createdBy: NumberLong(1),
    createdAt: new Date(),
    updatedAt: new Date()
  }
]);

// 示例练习题
db.exercises.insertMany([
  {
    lessonId: NumberLong(1),
    title: 'Hello World',
    description: '编写一个程序，输出 "Hello, World!"',
    language: 'java',
    difficulty: 'easy',
    starterCode: `public class HelloWorld {
    public static void main(String[] args) {
        // 在这里编写代码
    }
}`,
    solutionCode: `public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}`,
    testCases: [
      {
        input: '',
        expectedOutput: 'Hello, World!',
        isHidden: false,
        description: '输出 Hello, World!'
      }
    ],
    hints: [
      '使用 System.out.println() 方法输出文本',
      '注意字符串需要用双引号包围'
    ],
    timeLimit: 5,
    memoryLimit: 128000,
    tags: ['java', 'basic', 'hello-world'],
    createdAt: new Date(),
    updatedAt: new Date()
  }
]);

print('MongoDB 初始化完成！');
print('数据库: programming_platform_docs');
print('集合: lesson_content, code_templates, user_notes, exercises');
