# land

#### 项目介绍
画地块 sdk
#### 安装教程

1. 在项目根目录 gradle 文件中添加
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
2. 在 moudle 的 gradle 文件中添加依赖
```
dependencies {
	implementation 'com.gitee.wing-hawk:land:master-SNAPSHOT'
}
```
#### 使用说明

1. 将 mapview 添加到布局中
```xml
<com.hawk.map.impl.AmapView
	android:id="@+id/mapView"
	android:layout_width="match_parent"
	android:layout_height="match_parent"/>
```
2. 初始化
```java
AmapView mapView = findViewById(R.id.mapView);
MapManager mapManager = mapView.getMapManager();
PolygonPlotter polygonPlotter = new PolygonPlotter(ctx);
mapManager.addObserver(polygonPlotter);
```