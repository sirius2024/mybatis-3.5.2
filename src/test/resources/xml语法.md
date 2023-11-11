1、一对一:
<resultMap id="AccountResultMap" type="Account">
    <id property="aid" column="aid"/>
    <result property="name" column="name"/>
    <result property="id" column="id"/>
    <association property="details" column="aid" javaType="Details" select=""/>
  </resultMap>

2、id生成
keyProperty="id" keyColumn="id" useGeneratedKeys="true"

<selectKey keyColumn="id" keyProperty="id" order="AFTER" resultType="java.lang.Integer">
SELECT LAST_INSERT_ID()
</selectKey>

3、二级缓存
  <!--type：cache使用的类型，默认是PerpetualCache，这在一级缓存中提到过。
    eviction： 定义回收的策略，常见的有FIFO，LRU。
    flushInterval： 配置一定时间自动刷新缓存，单位是毫秒。
    size： 最多缓存对象的个数。
    readOnly： 是否只读，若配置可读写，则需要对应的实体类能够序列化。
    blocking： 若缓存中找不到对应的key，是否会一直blocking，直到有对应的数据进入缓存。
    -->
  <!--<cache></cache>-->


configuration元素：整个配置文件的根元素，包含多个子元素；
properties元素：用于配置属性，可以通过${}占位符引用属性值；
settings元素：用于配置MyBatis的全局设置，如缓存策略、延迟加载等；
typeAliases元素：用于配置类型别名，可以将Java类的全限定名映射成简短的别名，方便在XML文件中使用；
typeHandlers元素：用于配置类型处理器，可以将Java对象和数据库中的数据进行转换；
environments元素：用于配置环境，包括数据源和事务管理器；
mappers元素：用于配置Mapper接口，包括XML映射文件和注解方式的映射；
plugins元素：用于配置插件，可以在执行SQL语句前后进行一些自定义操作。