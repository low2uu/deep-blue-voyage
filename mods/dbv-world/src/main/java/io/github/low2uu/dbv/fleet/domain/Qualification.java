package io.github.low2uu.dbv.fleet.domain;

import java.util.List;
import java.util.ArrayList;

/** Admission is separate from the permanent vessel identity. Unknown never passes. */
public record Qualification(boolean controller, boolean oxygen, boolean sealed, boolean pending) {
    public boolean eligible() { return controller && oxygen && sealed && !pending; }
    public List<String> reasons() {
        var reasons = new ArrayList<String>();
        if (!controller) reasons.add("缺少有效船载电脑");
        if (!oxygen) reasons.add("缺少可工作的电解器或已启动并向密闭舱室供氧的扩散器");
        if (!sealed) reasons.add("未确认有效密闭舱室");
        if (pending) reasons.add("舱室扫描尚未完成、已过期或船体已变化，请稍后重试");
        return List.copyOf(reasons);
    }
}
