### 基于jacoco二开的再次开发说明
###### 1.修改了生成报告的模板，主要参考 [Jacoco-报告改造实践](https://testerhome.com/topics/27721) 
![输入图片说明](./readme/jacoco报告.png "屏幕截图.png")
###### 2.新增了调用的参数 --projectName(项目名称)、--appName(app名称)、--envName(环境名称)、--timeStr(报告生成日期，可不传，默认生成当天日期)，用于在报告中展示项目信息，用法参考
```
java -jar org.jacoco.cli-0.8.7-SNAPSHOT-nodeps.jar report jacoco.exec
--classfiles {classfiles} 
--sourcefiles {sourcefiles}
--html {html_report_path} 
--diffCode {diffcode} 
--projectName 项目1
--appName app1
--envName 测试环境
--timeStr 2021-01-01
--encoding=utf8
```
#### 原项目地址 https://gitee.com/Dray/jacoco
#### 以下是原jacoco二开介绍
***
### 简介
+ jacoco二开，主要加入了增量代码匹配的功能 具体设计方案参考  [jacoco增量代码实践](https://blog.csdn.net/tushuping/article/details/112613528) 

### 改造内容：
+ 新增类：所有新增类处于org.jacoco.core.internal.diff包下面；
+ 修改类：主要修改了核心类org.jacoco.core.analysis.Analyzer,org.jacoco.core.analysis.CoverageBuilder;org.jacoco.core.internal.flowClassProbesAdapter
+ 所有修改内容在commit记录可查，为了方便查看，我尽量减少了commit的次数

### 使用方法
###### 1、 下载源码，先运行mvn  spotless:apply 主要是jacoco进行了文件头文件校验，如果新加入了类或者修改了类，需要重新校验，所以要先运行此命令
###### 2、 然后运行maven: mvn clean package   -Dmaven.test.skip=true   -Dmaven.javadoc.skip=true
###### 3、 其中org.jacoco.cli-0.8.7-SNAPSHOT-nodeps.jar为构建出的我们需要的包（我已经构建好，懒得编译从这里下载[下载地址](https://gitee.com/Dray/jacoco/releases)）
![输入图片说明](https://images.gitee.com/uploads/images/2021/0401/140301_3d5bbe62_1007820.png "屏幕截图.png")
###### 4、 其他包都可以使用jacoco官方包，但是版本最好保持一致，也可以用我们自己构建出的包，report时使用我们构建的包，如果是增量覆盖率加入参数--diffCode=
```
"[{\"classFile\":\"com/dr/code/diff/config/GitConfig\",\"methodInfos\":[{\"methodName\":\"cloneRepository\",\"parameters\":\"String gitUrl,String codePath,String commitId\"},{\"methodName\":\"diffMethods\",\"parameters\":\"DiffMethodParams diffMethodParams\"},{\"methodName\":\"getClassMethods\",\"parameters\":\"String oldClassFile,String mewClassFile,DiffEntry diffEntry\"}],\"type\":\"MODIFY\"},{\"classFile\":\"com/dr/code/diff/controller/CodeDiffController\",\"methodInfos\":[{\"methodName\":\"getList\",\"parameters\":\"@ApiParam(required = true, name = \\\"gitUrl\\\", value = \\\"git远程仓库地址\\\") @RequestParam(value = \\\"gitUrl\\\") String gitUrl,@ApiParam(required = true, name = \\\"baseVersion\\\", value = \\\"git原始分支或tag\\\") @RequestParam(value = \\\"baseVersion\\\") String baseVersion,@ApiParam(required = true, name = \\\"nowVersion\\\", value = \\\"git现分支或tag\\\") @RequestParam(value = \\\"nowVersion\\\") String nowVersion\"}],\"type\":\"MODIFY\"},{\"classFile\":\"com/dr/code/diff/service/impl/CodeDiffServiceImpl\",\"methodInfos\":[{\"methodName\":\"getDiffCode\",\"parameters\":\"DiffMethodParams diffMethodParams\"}],\"type\":\"MODIFY\"},{\"classFile\":\"com/dr/common/utils/string/ScmStringUtil\",\"methodInfos\":[],\"type\":\"ADD\"}]"
```
###### 5、基于4新增了参数--diffCodeFiles=，支持读取json文件，diffCodeFiles的优先级要比diffCode高
```
--diffCodeFiles=/usr/xxx/xxx/xx.json
```
由于对象格式的通用性，可以配合 [差异代码获取](https://gitee.com/Dray/code-diff.git) 一起使用

#近期github不稳定，请访问https://gitee.com/Dray/jacoco.git


###### 3、关于生成多模块工程中jacoco报告命令参考 demo，其中diffcode传递的参数为diffcode服务返回的uniqueData字段，已进行转义和压缩直接使用 ：
```
java -jar org.jacoco.cli-0.8.7-SNAPSHOT-nodeps.jar    report jacoco.exec 
--classfiles \Desktop\feigin\web\build\classes   
--classfiles \Desktop\feigin\biz\build\classes   
--classfiles \Desktop\feigin\base\build\classes 
--sourcefiles \Desktop\feigin\web\src\main\java  
--sourcefiles \Desktop\feigin\biz\src\main\java   
--sourcefiles \Desktop\feigin\base\src\main\java --html report --xml jacoco.xml   
--diffCode="[{
      \"classFile\": \"com/vx/crm/web/swagger/SwaggerConfig\",
      \"methodInfos\": [
        {
          \"methodName\": \"apiInfo\",
          \"parameters\": \"\"
        }
      ],
      \"type\": \"MODIFY\"
    }]"
--encoding=utf8
```
###### 新增了diffCodeFiles参数，可以将--diffCode=替换为--diffCodeFiles=，后面指定json文件路径
###### 4、jacoco报告如存在乱码问题：  请执行命令时带入参数  --encoding=utf8
