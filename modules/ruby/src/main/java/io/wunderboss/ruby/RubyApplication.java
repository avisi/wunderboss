package io.wunderboss.ruby;

import io.wunderboss.Application;
import io.wunderboss.WunderBoss;
import io.wunderboss.ruby.rack.RackServlet;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RubyApplication extends Application {

    public RubyApplication(WunderBoss wunderBoss, Map<String, String> config) {
        super(wunderBoss, config);
        this.root = config.get("root");
        if (this.root.equals(".") && Ruby.getGlobalRuntime() != null) {
            // We're running embedded and the user is deploying the current app
            this.runtime = Ruby.getGlobalRuntime();
        } else {
            RubyInstanceConfig rubyConfig = new RubyInstanceConfig();
            rubyConfig.setLoadPaths(Arrays.asList(root));
            this.runtime = Ruby.newInstance(rubyConfig);
            this.runtime.setCurrentDirectory(root);
        }
    }

    @Override
    public void deployWeb(Map<String, String> config) throws Exception {
        if (!config.containsKey("static_dir")) {
            config.put("static_dir", this.root + "/public");
        }
        StringBuilder rackScript = new StringBuilder()
                .append("require 'rack'\n")
                .append("app, _ = Rack::Builder.parse_file(File.join('" + this.root + "', 'config.ru'))\n")
                .append("app\n");
        IRubyObject rackApplication = RuntimeHelper.evalScriptlet(this.runtime, rackScript.toString(), false);
        Map<String, Object> servletContextAttributes = new HashMap<>();
        servletContextAttributes.put("rack_application", rackApplication);
        getWundertow().deploy(RackServlet.class, servletContextAttributes, config);
    }

    private Ruby runtime;
    private String root;
}
