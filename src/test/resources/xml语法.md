1、一对一:

<resultMap id="AccountResultMap" type="Account">
    <id property="aid" column="aid"/>
    <result property="name" column="name"/>
    <result property="id" column="id"/>
    <association property="details" column="aid" javaType="Details" select=""/>
  </resultMap>