
## JWTUtil
我们利用 JWT 的工具类来生成我们的 token，这个工具类主要有生成 token 和 校验 token 两个方法

生成 token 时，指定 token 过期时间 ```EXPIRE_TIME``` 和签名密钥 ```SECRET```，然后将 date 和 username 写入 token 中，并使用带有密钥的 HS256 签名算法进行签名
 ```
Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
Algorithm algorithm = Algorithm.HMAC256(SECRET);
JWT.create()
    .withClaim("username", username)
    //到期时间
    .withExpiresAt(date)
    //创建一个新的JWT，并使用给定的算法进行标记
    .sign(algorithm);
```


## 数据库表
![user](https://upload-images.jianshu.io/upload_images/8807674-c67b741ce3f1f696.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
role: 角色；permission: 权限；ban: 封号状态
![role](https://upload-images.jianshu.io/upload_images/8807674-218309c1ee80b8fa.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

每个用户有对应的角色（user，admin），权限（normal，vip），而 user 角色默认权限为 normal， admin 角色默认权限为 vip（当然，user 也可以是 vip）

## 过滤器
在上一篇文章中，我们使用的是 shiro 默认的权限拦截 Filter，而因为 JWT 的整合，我们需要自定义自己的过滤器 JWTFilter，JWTFilter 继承了 BasicHttpAuthenticationFilter，并部分原方法进行了重写

该过滤器主要有三步：
1. 检验请求头是否带有 token ```((HttpServletRequest) request).getHeader("Token") != null```
2. 如果带有 token，执行 shiro 的 login() 方法，将 token 提交到 Realm 中进行检验；如果没有 token，说明当前状态为游客状态（或者其他一些不需要进行认证的接口）
```
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws UnauthorizedException {
        //判断请求的请求头是否带上 "Token"
        if (((HttpServletRequest) request).getHeader("Token") != null) {
            //如果存在，则进入 executeLogin 方法执行登入，检查 token 是否正确
            try {
                executeLogin(request, response);
                return true;
            } catch (Exception e) {
                //token 错误
                responseError(response, e.getMessage());
            }
        }
        //如果请求头不存在 Token，则可能是执行登陆操作或者是游客状态访问，无需检查 token，直接返回 true
        return true;
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String token = httpServletRequest.getHeader("Token");
        JWTToken jwtToken = new JWTToken(token);
        // 提交给realm进行登入，如果错误他会抛出异常并被捕获
        getSubject(request, response).login(jwtToken);
        // 如果没有抛出异常则代表登入成功，返回true
        return true;
    }
```
3. 如果在 token 校验的过程中出现错误，如 token 校验失败，那么我会将该请求视为认证不通过，则重定向到 ```/unauthorized/**```


另外，我将跨域支持放到了该过滤器来处理

## Realm 类
依然是我们的自定义 Realm ，对这一块还不了解的可以先看我的上一篇 shiro 的文章
- 身份认证
```
if (username == null || !JWTUtil.verify(token, username)) {
    throw new AuthenticationException("token认证失败！");
}
String password = userMapper.getPassword(username);
if (password == null) {
    throw new AuthenticationException("该用户不存在！");
}
int ban = userMapper.checkUserBanStatus(username);
if (ban == 1) {
    throw new AuthenticationException("该用户已被封号！");
}
```
拿到传来的 token ，检查 token 是否有效，用户是否存在，以及用户的封号情况

- 权限认证
```
SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
//获得该用户角色
String role = userMapper.getRole(username);
//每个角色拥有默认的权限
String rolePermission = userMapper.getRolePermission(username);
//每个用户可以设置新的权限
String permission = userMapper.getPermission(username);
Set<String> roleSet = new HashSet<>();
Set<String> permissionSet = new HashSet<>();
//需要将 role, permission 封装到 Set 作为 info.setRoles(), info.setStringPermissions() 的参数
roleSet.add(role);
permissionSet.add(rolePermission);
permissionSet.add(permission);
//设置该用户拥有的角色和权限
info.setRoles(roleSet);
info.setStringPermissions(permissionSet);
```
利用 token 中获得的 username，分别从数据库查到该用户所拥有的角色，权限，存入 SimpleAuthorizationInfo 中

## ShiroConfig 配置类
设置好我们自定义的 filter，并使所有请求通过我们的过滤器，除了我们用于处理未认证请求的 ```/unauthorized/**```
```
@Bean
public ShiroFilterFactoryBean factory(SecurityManager securityManager) {
    ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();

    // 添加自己的过滤器并且取名为jwt
    Map<String, Filter> filterMap = new HashMap<>();
    //设置我们自定义的JWT过滤器
    filterMap.put("jwt", new JWTFilter());
    factoryBean.setFilters(filterMap);
    factoryBean.setSecurityManager(securityManager);
    Map<String, String> filterRuleMap = new HashMap<>();
    // 所有请求通过我们自己的JWT Filter
    filterRuleMap.put("/**", "jwt");
    // 访问 /unauthorized/** 不通过JWTFilter
    filterRuleMap.put("/unauthorized/**", "anon");
    factoryBean.setFilterChainDefinitionMap(filterRuleMap);
    return factoryBean;
}
```
## 权限控制注解 @RequiresRoles， @RequiresPermissions
这两个注解为我们主要的权限控制注解, 如
```
// 拥有 admin 角色可以访问
@RequiresRoles("admin")
```
```
// 拥有 user 或 admin 角色可以访问
@RequiresRoles(logical = Logical.OR, value = {"user", "admin"})
```
```
// 拥有 vip 和 normal 权限可以访问
@RequiresPermissions(logical = Logical.AND, value = {"vip", "normal"})
```
```
// 拥有 user 或 admin 角色，且拥有 vip 权限可以访问
@GetMapping("/getVipMessage")
@RequiresRoles(logical = Logical.OR, value = {"user", "admin"})
@RequiresPermissions("vip")
public ResultMap getVipMessage() {
    return resultMap.success().code(200).message("成功获得 vip 信息！");
}
```
当我们写的接口拥有以上的注解时，如果请求没有带有 token 或者带了 token 但权限认证不通过，则会报 UnauthenticatedException 异常，但是我在 ExceptionController 类对这些异常进行了集中处理
```
@ExceptionHandler(ShiroException.class)
public ResultMap handle401() {
    return resultMap.fail().code(401).message("您没有权限访问！");
}
```
这时，出现 shiro 相关的异常时则会返回
```
{
    "result": "fail",
    "code": 401,
    "message": "您没有权限访问！"
}
```
除了以上两种，还有 @RequiresAuthentication ，@RequiresUser 等注解

## 功能实现
用户角色分为三类，管理员 admin，普通用户 user，游客 guest；admin 默认权限为 vip，user 默认权限为 normal，当 user 升级为 vip 权限时可以访问 vip 权限的页面。

具体实现可以看源代码（开头已经给出地址）

### 登陆
登陆接口不带有 token，当登陆密码，用户名验证正确后返回 token。
```
@PostMapping("/login")
public ResultMap login(@RequestParam("username") String username,
                       @RequestParam("password") String password) {
    String realPassword = userMapper.getPassword(username);
    if (realPassword == null) {
        return resultMap.fail().code(401).message("用户名错误");
    } else if (!realPassword.equals(password)) {
        return resultMap.fail().code(401).message("密码错误");
    } else {
        return resultMap.success().code(200).message(JWTUtil.createToken(username));
    }
}
```
```
{
    "result": "success",
    "code": 200,
    "message": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1MjUxODQyMzUsInVzZXJuYW1lIjoiaG93aWUifQ.fG5Qs739Hxy_JjTdSIx_iiwaBD43aKFQMchx9fjaCRo"
}
```

## 异常处理
```
    // 捕捉shiro的异常
    @ExceptionHandler(ShiroException.class)
    public ResultMap handle401() {
        return resultMap.fail().code(401).message("您没有权限访问！");
    }

    // 捕捉其他所有异常
    @ExceptionHandler(Exception.class)
    public ResultMap globalException(HttpServletRequest request, Throwable ex) {
        return resultMap.fail()
                .code(getStatus(request).value())
                .message("访问出错，无法访问: " + ex.getMessage());
    }
```

## 权限控制
- UserController（user 或 admin 可以访问）
在接口上带上 ```@RequiresRoles(logical = Logical.OR, value = {"user", "admin"})```
  - vip 权限
再加上```@RequiresPermissions("vip")```

- AdminController（admin 可以访问）
在接口上带上 ```@RequiresRoles("admin")```

- GuestController（所有人可以访问）
不做权限处理

## 测试结果
![不带 token](https://upload-images.jianshu.io/upload_images/8807674-8df75027832d8742.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![带上 token](https://upload-images.jianshu.io/upload_images/8807674-8c2736e7daa1e303.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![带上错误的 token](https://upload-images.jianshu.io/upload_images/8807674-79716d898d6e3359.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![游客，无 token](https://upload-images.jianshu.io/upload_images/8807674-499c44828b453767.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![访问无权限的接口（vip）](https://upload-images.jianshu.io/upload_images/8807674-b4087f6b38c32f11.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![该用户已被封号](https://upload-images.jianshu.io/upload_images/8807674-555a7133a52d95a4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

