# Indoqa XML

Indoqa XML provides pipeline-based XML processing and XML tools. It reuses parts of 
[Apache Cocoon 3](http://cocoon.apache.org//3.0/) (cocoon-pipeline, cocoon-pipeline-sax) 
and the [Cocoon XML](http://svn.apache.org/repos/asf/cocoon/subprojects/cocoon-xml/trunk/) subproject.

## Pipeline DSL

### Transform XML and get an XML string

```java
String result = xmlPipeline()
    .of("<x></x>")
    .transformWithXSLT(this.getClass().getResource("/test.xslt"))
    .asXmlString();
```

### Transform XML and get a DOM document

```java
Document result = xmlPipeline()
    .of("<x></x>")
    .transformWithXSLT(this.getClass().getResource("/test.xslt"))
    .asDocument();
```
