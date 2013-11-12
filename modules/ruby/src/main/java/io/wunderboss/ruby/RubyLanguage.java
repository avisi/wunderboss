package io.wunderboss.ruby;

import io.wunderboss.Language;
import io.wunderboss.Options;
import io.wunderboss.WunderBoss;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.RubyProc;

import java.util.Arrays;

public class RubyLanguage implements Language {

    @Override
    public void initialize(WunderBoss container) {

    }

    @Override
    public Ruby getRuntime(Options options) {
        String root = options.get("root", ".").toString();
        if (root.equals(".")) {
            return Ruby.getGlobalRuntime();
        } else {
            RubyInstanceConfig rubyConfig = new RubyInstanceConfig();
            rubyConfig.setLoadPaths(Arrays.asList(root));
            Ruby runtime = Ruby.newInstance(rubyConfig);
            runtime.setCurrentDirectory(root);
            return runtime;
        }
    }

    @Override
    public void destroyRuntime(Object runtime) {
        ((Ruby) runtime).tearDown(false);
    }

    @Override
    public Options transformOptions(Options options) {
        Options convertedOptions = new Options();
        for (String key : options.keySet()) {
            Object value = options.get(key);
            if (value instanceof RubyProc) {
                value = ((RubyProc) value).toJava(Runnable.class);
            }
            convertedOptions.put(key, value);
        }
        return convertedOptions;
    }
}