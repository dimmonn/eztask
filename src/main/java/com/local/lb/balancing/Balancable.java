package com.local.lb.balancing;

import com.local.lb.model.Host;
import com.local.lb.servlet.Request;

import java.util.List;

public interface Balancable {
    Host balance(List<Host> hosts, Request request);
}
