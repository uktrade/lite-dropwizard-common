# Readiness Metric

Provides an HttpServlet to facilitate readiness probes as used by Kubernetes for a pod lifecycle, you can find more information on the topic [here](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-probes/#define-readiness-probes)

A readiness probe endpoint (such as /ready) functions in the following way:
* GET `/ready` -> responds with `200 (OK)`: The application is ready to receive requests
* GET `/ready` -> responds with `500 (BAD REQUEST)`: The application is not ready to receive requests

This probe is typically used during the startup sequence of an application, and describes a state where by the application is functioning (i.e. a liveness check returns OK) but not ready to deal with inbound requests.

See the following typical scenario:
1. Application is starting: `liveness -> false`, `readiness -> false`
2. Application has started, but needs to load in data from an external service: `liveness -> true`, `readiness -> false`
3. Application has fully loaded the data: `liveness -> true`, `readiness -> true`

## Readiness vs liveness
Kubernetes deals with liveness and readiness checks in two very distinct way:

Liveness: `true` -> the pod will stay online, `false` -> the pod will be restarted

Readiness: `true` -> the pod will receive traffic, `false` -> the pod will not receive traffic

A liveness check determines the life cycle of the pod, a failing check will kill the pod. A readiness check represents 
the ability of the pod to responds to normal requests (i.e. not metric or admin endpoints). A pod can flip between 
`ready -> true` and `ready -> false` many times over it's lifetime, this is not the case for liveness.   

## Setup
The following assumes you're using this repo in DropWizard 1.0+ with Guice for DI.

Add this project as a compile dependency, in gradle this is:
```
compile 'uk.gov.bis.lite:readiness-metric:1.0'
```

Add the `ReadinessServlet` to the `Application#run` method body, binding it to endpoint `/ready` on the admin port
```java
  public void run(MainApplicationConfiguration configuration, Environment environment) {
    ...
    ReadinessServlet readinessServlet = injector.getInstance(ReadinessServlet.class);
    environment.admin().addServlet("ready", readinessServlet).addMapping("/ready");
    ...
  }
```

Then in your `GuiceModule`, add a provider for `ReadinessService`. In this example we're using `DefaultReadinessService` 
which already responds as `true/ready`, you should create your own `XYZReadiness` class which implements `ReadinessService`. 
```java
  @Provides
  public ReadinessService provideReadinessService() {
    return new DefaultReadinessService();
  }
```

To check the endpoint is correctly configured with the default implementation send a `GET` request to the admin port (`8081`) 
and `/ready` endpoint. e.g. `GET http://hostname:8081/ready` which should respond with `200 (OK)` and a JSON body 
```json
{
  "ready" : true
}
```