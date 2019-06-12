package ngoy.router;

import ngoy.core.Inject;

import java.util.List;

public class DefaultActiveRouteProvider implements ActiveRouteProvider {

    @Inject
    public RouterConfig config;

    @Override
    public ActiveRoute getActiveRoute(String path) {
        String base = config.getBaseHref();

        if (base.length() > path.length() || !path.startsWith(base)) {
            return null;
        }

        String sub = path.substring(base.length());
        if (sub.isEmpty()) {
            return null;
        }

        if (!base.equals("/")) {
            sub = sub.substring(1); // remove slash
        }

        String[] subSplits = sub.split("/");

        List<Route> routes = config.getRoutes();
        for (int i = 0, n = routes.size(); i < n; i++) {
            Route route = routes.get(i);
            String[] splits = route.getPath().split("/");

            if (splits[0].equals(subSplits[0])) {
                RouteParams params = new RouteParams();
                if (splits.length > 1) {
                    String param = splits[1];
                    if (param.startsWith(":")) {
                        param = param.substring(1);
                    }
                    String value = subSplits.length > 1 ? subSplits[1] : null;
                    params.put(param, value);
                }

                return new ActiveRoute(i, params);
            }
        }

        return null;
    }
}
