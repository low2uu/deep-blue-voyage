(function () {
  "use strict";
  const C = ContentCatalog, S = ContentSpec, KEY = "dbv.contentDesigner.v1";
  const $ = id => document.getElementById(id);
  let doc = S.newDocument(), active = C.groups[0].id, saveTimer;
  const statusLabels = { specified: "明确指定", inherit: "继承参考", pending: "待设计", not_applicable: "不适用" };
  function el(tag, text, className) {
    const e = document.createElement(tag);
    if (text !== undefined) e.textContent = text;
    if (className) e.className = className;
    return e;
  }
  function message(text, kind = "") { $("message").textContent = text; $("message").className = kind; }
  function edited() {
    doc.metadata.updatedAt = new Date().toISOString();
    clearTimeout(saveTimer);
    saveTimer = setTimeout(() => {
      try { localStorage.setItem(KEY, JSON.stringify(doc)); message("草稿已保存到当前浏览器。", "success"); }
      catch (_) { message("浏览器无法保存草稿，请导出JSON保存到本机。", "warnings"); }
    }, 350);
    renderNav();
    renderValidation(false);
  }
  function valueControl(f, value, changed, id) {
    let input;
    if (f.type === "rows") {
      input = el("div"); input.id = id;
      const list = Array.isArray(value) ? value : [];
      list.forEach((row, ix) => {
        const card = el("div", undefined, "row"); card.append(el("strong", `第 ${ix + 1} 条`));
        for (const col of f.columns) {
          const subId = `${id}-${ix}-${col.id}`, label = el("label", col.label); label.htmlFor = subId;
          card.append(label, valueControl(col, row[col.id], v => { row[col.id] = v; changed(list); }, subId));
        }
        const remove = el("button", "移除这条"); remove.type = "button";
        remove.onclick = () => { list.splice(ix, 1); changed(list); renderForm(); };
        card.append(remove); input.append(card);
      });
      const add = el("button", "＋ 添加一条"); add.type = "button";
      add.onclick = () => {
        if (list.length >= 500) return message("单项最多500条记录。", "errors");
        list.push(Object.fromEntries(f.columns.map(c => [c.id, S.emptyValue(c)])));
        changed(list); renderForm();
      };
      input.append(add);
      return input;
    }
    if (f.type === "enum" || f.type === "boolean") {
      input = el("select");
      const choices = f.type === "boolean" ? ["true|是", "false|否"] : f.options;
      choices.forEach(choice => { const [v, name] = choice.split("|"); const option = el("option", name); option.value = v; input.append(option); });
      input.value = String(value ?? (f.type === "boolean" ? false : f.options[0].split("|")[0]));
    } else if (["textarea", "lines"].includes(f.type)) {
      input = el("textarea"); input.value = f.type === "lines" ? (value || []).join("\n") : (value ?? "");
      if (f.type === "lines") input.placeholder = "每行一项";
    } else {
      input = el("input"); input.type = ["number", "integer"].includes(f.type) ? "number" : "text";
      if (input.type === "number") { input.step = f.type === "integer" ? "1" : "any"; if (f.min !== undefined) input.min = f.min; if (f.max !== undefined) input.max = f.max; }
      input.value = value ?? ""; input.placeholder = f.placeholder || "";
    }
    input.id = id;
    input.addEventListener("input", () => {
      let v = input.value;
      if (f.type === "boolean") v = v === "true";
      if (["number", "integer"].includes(f.type)) v = v === "" ? null : Number(v);
      if (f.type === "lines") v = v.split(/\r?\n/).map(x => x.trim()).filter(Boolean);
      changed(v);
    });
    return input;
  }
  function renderField(g, f) {
    const r = doc.sections[g.id][f.id], id = `field-${g.id}-${f.id}`;
    const card = el("section", undefined, "field"), head = el("div", undefined, "field-head");
    const label = el("label", f.label); label.htmlFor = id;
    if (f.essential) label.append(el("span", "必填", "required"));
    const state = el("select"); state.setAttribute("aria-label", `${f.label}的确定状态`);
    for (const [k, v] of Object.entries(statusLabels)) { const o = el("option", v); o.value = k; state.append(o); }
    state.value = r.status;
    state.onchange = () => {
      if (["pending", "not_applicable"].includes(state.value) && r.value !== null && !confirm("切换为待设计或不适用会清空这一项的具体值，保留来源与备注。继续吗？")) { state.value = r.status; return; }
      r.status = state.value;
      r.value = ["pending", "not_applicable"].includes(r.status) ? null : (r.status === "inherit" ? r.value : (r.value ?? S.emptyValue(f)));
      edited(); renderForm();
    };
    head.append(label, state); card.append(head, el("div", `${g.id}.${f.id}`, "field-code"));
    if (["specified", "inherit"].includes(r.status)) {
      if (r.status === "inherit") {
        card.append(el("p", "参考来源必填；只在核对过后记录基线值。实际跟随规则请在备注中说明。", "help"));
        const baselineLabel = el("label", undefined, "check"), baseline = el("input"); baseline.type = "checkbox"; baseline.checked = r.value !== null;
        baseline.onchange = () => { r.value = baseline.checked ? S.emptyValue(f) : null; edited(); renderForm(); };
        baselineLabel.append(baseline, document.createTextNode("记录已核对的基线值")); card.append(baselineLabel);
      }
      if (r.status === "specified" || r.value !== null) card.append(valueControl(f, r.value, value => { r.value = value; edited(); }, id));
    } else card.append(el("p", r.status === "pending" ? "尚未决定；导出时明确保留为待定。" : "此设计不涉及该项。", "inactive"));
    const details = el("details", undefined, "field-details"); details.open = r.status === "inherit";
    details.append(el("summary", "参考来源与补充说明"));
    const referenceLabel = el("label", "参考方块 / Mod / 文档来源"); referenceLabel.htmlFor = `${id}-ref`;
    const reference = el("input"); reference.id = `${id}-ref`; reference.value = r.reference; reference.placeholder = "例如 minecraft:obsidian";
    reference.oninput = () => { r.reference = reference.value; edited(); };
    const notesLabel = el("label", "备注、单位、继承范围与例外"); notesLabel.htmlFor = `${id}-notes`;
    const notes = el("textarea"); notes.id = `${id}-notes`; notes.value = r.notes; notes.oninput = () => { r.notes = notes.value; edited(); };
    details.append(referenceLabel, reference, notesLabel, notes); card.append(details);
    return card;
  }
  function renderNav() {
    $("nav").replaceChildren();
    let known = 0, total = 0;
    for (const g of C.groups) {
      const count = g.fields.filter(f => doc.sections[g.id][f.id].status !== "pending").length;
      known += count; total += g.fields.length;
      const button = el("button", g.title, active === g.id ? "active" : "");
      button.append(el("small", `${count}/${g.fields.length}`));
      button.onclick = () => { active = g.id; $("search").value = ""; renderNav(); renderForm(); };
      $("nav").append(button);
    }
    const ext = el("button", `扩展需求 (${doc.extensions.length})`, active === "extensions" ? "active" : "");
    ext.onclick = () => { active = "extensions"; $("search").value = ""; renderNav(); renderForm(); }; $("nav").append(ext);
    $("progress").textContent = `${known} / ${total} 项已标明状态（含继承与不适用）。未明确的项目会完整保留。`;
  }
  function renderForm() {
    const form = $("form"); form.replaceChildren();
    const query = $("search").value.trim().toLowerCase();
    if (active === "extensions" && !query) {
      form.append(el("h2", "扩展需求"), el("p", "表中未覆盖的功能可写在这里；为每条补上期望结果，避免需求遗漏。", "help"));
      form.append(valueControl({ type: "rows", columns: [
        { id: "key", label: "需求名称", type: "text" }, { id: "description", label: "完整描述", type: "textarea" }, { id: "acceptance", label: "验收要求", type: "textarea" }
      ] }, doc.extensions, v => { doc.extensions = v; edited(); }, "extensions"));
      return;
    }
    let count = 0;
    for (const g of C.groups) {
      if (!query && g.id !== active) continue;
      const fields = g.fields.filter(f => (!query || `${f.label} ${f.id} ${g.title}`.toLowerCase().includes(query)) && (!$("pending").checked || doc.sections[g.id][f.id].status === "pending"));
      if (!fields.length) continue;
      form.append(el("span", g.id, "section-badge"), el("h2", g.title), el("p", g.help, "help"));
      fields.forEach(f => { form.append(renderField(g, f)); count++; });
    }
    if (!count) form.append(el("p", "没有符合当前筛选条件的属性。"));
  }
  function renderValidation(full = true) {
    const result = S.validate(doc), box = $("validation"); box.replaceChildren();
    box.append(el("p", `${result.errors.length} 项错误 · ${result.warnings.length} 项提示`, result.errors.length ? "errors" : "success"));
    if (full) for (const [kind, entries] of [["errors", result.errors], ["warnings", result.warnings]]) {
      const list = el("ul", undefined, kind); entries.forEach(text => list.append(el("li", text))); box.append(list);
    }
    return result;
  }
  function download(name, value) {
    const url = URL.createObjectURL(new Blob([JSON.stringify(value, null, 2) + "\n"], { type: "application/json;charset=utf-8" }));
    const a = el("a"); a.href = url; a.download = name; document.body.append(a); a.click(); a.remove(); setTimeout(() => URL.revokeObjectURL(url), 10000);
  }
  function filename(suffix) {
    const id = doc.sections.identity.registryId.value || "new-content";
    return String(id).replace(/[^a-zA-Z0-9._-]/g, "-") + suffix + ".json";
  }
  function replace(next) { clearTimeout(saveTimer); doc = next; active = "identity"; $("search").value = ""; $("pending").checked = false; edited(); renderNav(); renderForm(); renderValidation(); }
  $("new").onclick = () => { if (confirm("新建设计会替换当前页面内容。需要保留时请先导出草稿。继续吗？")) replace(S.newDocument()); };
  $("preset").onclick = () => { if (confirm("载入预设会替换当前设计。这个预设是目标方案，不代表游戏已经实现。继续吗？")) replace(S.hullPreset()); };
  $("monster-preset").onclick = () => { if (confirm("新建怪物会替换当前设计，请先导出需要保留的草稿。继续吗？")) replace(S.monsterPreset()); };
  $("search").oninput = renderForm; $("pending").onchange = renderForm;
  $("import").onclick = () => $("file").click();
  $("file").onchange = async () => {
    const file = $("file").files[0]; if (!file) return;
    try {
      if (file.size > 5 * 1024 * 1024) throw new Error("文件超过5 MiB，请分拆设计");
      const next = S.importDocument(JSON.parse((await file.text()).replace(/^\uFEFF/, "")));
      const errors = S.structuralErrors(next, true); if (errors.length) throw new Error(errors.slice(0, 8).join("\n"));
      if (confirm("导入会替换当前设计。继续吗？")) { replace(next); message("导入成功；所有字段、状态、来源和备注均已保留。", "success"); }
    } catch (e) { message(`未导入，当前设计未改变：\n${e.message}`, "errors"); }
    $("file").value = "";
  };
  $("validate").onclick = () => renderValidation();
  $("export").onclick = () => {
    const result = renderValidation();
    if (result.errors.length) return message("请先处理交接检查中的错误；也可以导出草稿保留未完成的设计。", "errors");
    doc.metadata.updatedAt = new Date().toISOString(); download(filename("-spec"), doc);
    message(`已导出开发规格，包含 ${result.pending || 0} 项待设计内容。请把JSON交给开发者；本工具没有修改游戏。`, "success");
  };
  $("draft").onclick = () => { download(filename("-draft"), doc); message("草稿已导出，可再次导入继续编辑；它可能尚未通过开发交接检查。"); };
  $("schema").onclick = () => download("content-spec.schema.json", S.schema());
  $("preview").onclick = () => { $("json-preview").value = JSON.stringify(doc, null, 2); $("json-dialog").showModal(); };
  $("close-preview").onclick = () => $("json-dialog").close();
  $("clear").onclick = () => {
    if (!confirm("仅清除浏览器保存的草稿，当前页面和已下载文件会保留。下次编辑会重新保存。继续吗？")) return;
    clearTimeout(saveTimer);
    try { localStorage.removeItem(KEY); message("浏览器草稿已清除，当前页面仍保留。"); } catch (_) { message("浏览器不允许访问草稿存储。"); }
  };
  try {
    const stored = localStorage.getItem(KEY);
    if (stored) {
      const next = S.importDocument(JSON.parse(stored));
      if (!S.structuralErrors(next, true).length) { doc = next; message("已恢复当前浏览器中的草稿。"); }
      else message("旧草稿格式不兼容，未覆盖也未自动迁移；请从此前导出的JSON恢复。", "warnings");
    }
  } catch (_) { message("浏览器草稿不可用，请使用导出保存。", "warnings"); }
  renderNav(); renderForm(); renderValidation(false);
})();
