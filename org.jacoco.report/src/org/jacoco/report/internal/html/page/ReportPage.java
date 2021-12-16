/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.internal.html.page;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.analysis.*;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.IHTMLReportContext;
import org.jacoco.report.internal.html.ILinkable;
import org.jacoco.report.internal.html.resources.Resources;
import org.jacoco.report.internal.html.resources.Styles;

/**
 * Base class for HTML page generators. It renders the page skeleton with the
 * breadcrumb, the title and the footer. Every report page is part of a
 * hierarchy and has a parent page (except the root page).
 */
public abstract class ReportPage implements ILinkable {

	private final ReportPage parent;

	/**
	 * output folder for this node
	 */
	protected final ReportOutputFolder folder;

	/**
	 * context for this report
	 */
	protected final IHTMLReportContext context;

	protected final DecimalFormat df = new DecimalFormat("0.00");

	/**
	 * Creates a new report page.
	 *
	 * @param parent
	 *            optional hierarchical parent
	 * @param folder
	 *            base folder to create this report in
	 * @param context
	 *            settings context
	 */
	protected ReportPage(final ReportPage parent,
			final ReportOutputFolder folder, final IHTMLReportContext context) {
		this.parent = parent;
		this.context = context;
		this.folder = folder;
	}

	/**
	 * Checks whether this is the root page of the report.
	 *
	 * @return <code>true</code> if this is the root page
	 */
	protected final boolean isRootPage() {
		return parent == null;
	}

	// private String analysisLocation(String fileName,String folderPath) {
	// return null;
	// }

	public abstract Object getResultNode();

	public Object getSummaryNode() {
		return getResultNode();
	}

	protected void creatTitleArea(HTMLElement graphDiv, ICoverageNode summary,
			String projectName, String appName, String envName, String timeStr,
			String locationStr) throws IOException {
		final HTMLElement blank = graphDiv.span("blank");
		blank.attr("style", "height:20px");
		blank.text("");
		blank.element("br");

		final HTMLElement project = graphDiv.span("project");
		project.attr("style",
				"font-size: large;color: dimgray;margin-left:30px;");
		project.text(projectName + " - 代码覆盖率报告");

		final HTMLElement location = graphDiv.span("location");
		location.attr("style",
				"font-size:xx-small;color: gray;float:right;margin-right:10px;");
		location.text("统计对象：" + locationStr);
		graphDiv.element("br");

		final HTMLElement time = graphDiv.span("time");
		time.attr("style",
				"font-size:xx-small;color: gray;float:right;margin-right:10px;");
		if (timeStr == null || timeStr.equals("")) {
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
			timeStr = sf.format(new Date());
		}
		time.text(timeStr);
		graphDiv.element("br");

		final HTMLElement app = graphDiv.span("app");
		app.attr("style",
				"font-size: xx-small;background-color:#5F6368;color: white;margin-left:30px;");
		app.text("[ " + appName + " ]");

		final HTMLElement env = graphDiv.span("env");
		env.attr("style",
				"font-size: xx-small;background-color:#AAAAAA;color: white;margin-left:60px;");
		env.text("[ " + envName + " ]");

		graphDiv.element("br");
		graphDiv.element("br");

		final HTMLElement collectParentDiv = graphDiv.div("");
		collectParentDiv.attr("style", "text-align:center;");

		final HTMLElement collectFirstDiv = collectParentDiv.div("");
		collectFirstDiv.attr("style",
				"margin-right:10%;margin-left:20%; display:inline-block;");

		final HTMLElement lineCollect = collectFirstDiv.span("lineCollect");
		ICounter lineCounter = summary.getLineCounter();
		int lineCoveredCount = lineCounter.getCoveredCount();
		int lineTotalCount = lineCounter.getTotalCount();
		lineCollect.attr("style",
				"width:auto; display:block; text-align:left;color: #1AAD19;");
		lineCollect.text("行覆盖统计：" + lineCoveredCount + "/" + lineTotalCount);

		ICounter branchCounter = summary.getBranchCounter();
		int branchCoveredCount = branchCounter.getCoveredCount();
		int branchTotalCount = branchCounter.getTotalCount();
		final HTMLElement branchCollect = collectFirstDiv.span("branchCollect");
		branchCollect.attr("style",
				"width:auto; display:block; text-align:left;color: #1AAD19;");
		branchCollect
				.text("分支覆盖统计：" + branchCoveredCount + "/" + branchTotalCount);

		final HTMLElement collectSecDiv = collectParentDiv.div("");
		collectSecDiv.attr("style",
				"margin-right:20%;margin-left:10%; display:inline-block;");

		final HTMLElement methodCollect = collectSecDiv.span("methodCollect");
		ICounter methodCounter = summary.getMethodCounter();
		int methodCoveredCount = methodCounter.getCoveredCount();
		int methodTotalCount = methodCounter.getTotalCount();
		methodCollect.attr("style",
				"width:auto; display:block; text-align:left;color: #1AAD19;");
		methodCollect
				.text("方法覆盖统计：" + methodCoveredCount + "/" + methodTotalCount);

		final HTMLElement classCollect = collectSecDiv.span("classCollect");
		ICounter classCounter = summary.getClassCounter();
		int classCoveredCount = classCounter.getCoveredCount();
		int classTotalCount = classCounter.getTotalCount();
		classCollect.attr("style",
				"width:auto; display:block; text-align:left;color: #1AAD19;");
		classCollect.text("类覆盖统计：" + classCoveredCount + "/" + classTotalCount);

	}

	private void summary(HTMLElement graphBody, ICoverageNode summary,
			String projectName, String appName, String envName) {
		try {
			HTMLElement containers = graphBody.div("container");
			containers.attr("style",
					"width: 100%;height: 200px;text-align: center;margin: 0 auto;");

			// 生成行覆盖率图标
			ICounter lineCounter = summary.getLineCounter();
			HTMLElement div1 = containers.div("container1");
			drawGraph(div1, graphBody, "1", "行 ", lineCounter.getCoveredCount(),
					lineCounter.getMissedCount(),
					lineCounter.getCoveredRatio());

			// 生成分支覆盖率图标
			ICounter branchCounter = summary.getBranchCounter();
			HTMLElement div2 = containers.div("container2");
			drawGraph(div2, graphBody, "2", "分支 ",
					branchCounter.getCoveredCount(),
					branchCounter.getMissedCount(),
					branchCounter.getCoveredRatio());

			// 生成方法覆盖率图标
			ICounter methodCounter = summary.getMethodCounter();
			HTMLElement div3 = containers.div("container3");
			drawGraph(div3, graphBody, "3", "方法 ",
					methodCounter.getCoveredCount(),
					methodCounter.getMissedCount(),
					methodCounter.getCoveredRatio());

			// 生成类覆盖率图标
			ICounter classCounter = summary.getClassCounter();
			HTMLElement div4 = containers.div("container4");
			drawGraph(div4, graphBody, "4", "类 ",
					classCounter.getCoveredCount(),
					classCounter.getMissedCount(),
					classCounter.getCoveredRatio());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void drawGraph(HTMLElement divItem, HTMLElement grappBody,
			String index, String title, int cover, int miss, double percent) {
		try {
			divItem.attr("style",
					"min-width:200px;height:200px;display: inline-block;");
			divItem.attr("id", "container" + index);
			if (Double.isNaN(percent))
				percent = 0.0000;

			divItem.scriptContent("Highcharts.setOptions({\n"
					+ "    colors: ['#67C23A', '#C0C4CC']\n" + "});\n"
					+ "var chart = Highcharts.chart({\n" + "    chart: {\n"
					+ "        spacing: [10, 0, 10, 0],\n"
					+ "        renderTo: 'container" + index + "',\n"
					+ "        height: 200,\n" + "        width: 200\n"
					+ "    },\n" + "    exporting: {\n"
					+ "        enabled: false\n" + "    },\n"
					+ "    credits: {\n" + "        enabled: false\n"
					+ "    },\n" + "    title: {\n"
					+ "        floating: true,\n" + "        text: '" + title
					+ Double.parseDouble(df.format(percent * 100)) + "%',\n"
					+ "        style: {\"fontSize\": '12px'}\n" + "    },\n"
					+ "    tooltip: {\n" + "        formatter: function () {\n"
					+ "            return '<b>' + this.point.name +'" + title
					+ ":</b> ' + Highcharts.numberFormat(this.y, 0, '.');\n"
					+ "        }\n" + "    },\n" + "    plotOptions: {\n"
					+ "        pie: {\n" + "            size: '80%',\n"
					+ "            borderWidth: 0,\n"
					+ "            allowPointSelect: false,\n"
					+ "            cursor: 'pointer',\n"
					+ "            dataLabels: {\n"
					+ "                enabled: false,\n"
					+ "                format: '<b>{point.name}</b>: {point.percentage:.1f} %',\n"
					+ "                style: {\n"
					+ "                    color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'\n"
					+ "                }\n" + "            }\n" + "        }\n"
					+ "    },\n" + "    series: [{\n" + "        type: 'pie',\n"
					+ "        innerSize: '80%',\n" + "        name: '" + title
					+ "',\n" + "        data: [\n" + "            ['已覆盖', "
					+ cover + "],\n" + "            ['未覆盖', " + miss + "]\n"
					+ "        ]\n" + "    }]\n" + "}, function (c) {\n"
					+ "    var centerY = c.series[0].center[1],\n"
					+ "        titleHeight = parseInt(c.title.styles.fontSize);\n"
					+ "    c.setTitle({\n"
					+ "        y: centerY + titleHeight / 2\n" + "    });\n"
					+ "});");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Renders this page's content and optionally additional pages. This method
	 * must be called at most once.
	 *
	 * @param projectName
	 * @param appName
	 * @param envName
	 * @param timeStr
	 * @throws IOException
	 *             if the page can't be written
	 */
	public void render(String projectName, String appName, String envName,
			String timeStr) throws IOException {
		final HTMLElement html = new HTMLElement(
				folder.createFile(getFileName()), context.getOutputEncoding());
		html.attr("lang", context.getLocale().getLanguage());
		head(html.head());
		// 计算正在渲染的对象
		String location = getLinkLabel();
		// 添加jquery
		html.script("http://apps.bdimg.com/libs/jquery/2.1.4/jquery.min.js");
		// 添加bootstrap
		HTMLElement bootstrap = html.link("stylesheet",
				"https://cdn.staticfile.org/twitter-bootstrap/3.3.7/css/bootstrap.min.css",
				"text/css");
		// 为每个报告页面添加highcharts js
		html.script("https://code.highcharts.com.cn/highcharts/highcharts.js");
		html.script(
				"https://code.highcharts.com.cn/highcharts/modules/exporting.js");
		body(html.body(), projectName, appName, envName, timeStr,
				getFileName().contains(".java"), location);
		html.close();
	}

	/**
	 * Creates the elements within the head element.
	 *
	 * @param head
	 *            head tag of the page
	 * @throws IOException
	 *             in case of IO problems with the report writer
	 */
	protected void head(final HTMLElement head) throws IOException {
		head.meta("Content-Type", "text/html;charset=UTF-8");
		head.link("stylesheet",
				context.getResources().getLink(folder, Resources.STYLESHEET),
				"text/css");
		// 自定义的菜单样式
		head.link("stylesheet",
				context.getResources().getLink(folder, Resources.MENU_STYLE),
				"text/css");
		head.link("shortcut icon",
				context.getResources().getLink(folder, "report.gif"),
				"image/gif");
		head.title().text(getLinkLabel());
	}

	/**
	 * @param left
	 *            左侧元素节点
	 * @throws IOException
	 */
	private void creatMenuArea(HTMLElement left) throws IOException {
		// 树形菜单
		final HTMLElement tree = left.div("tree");
		tree.scriptContent("$(function () {\n"
				+ "        $('#search').bind('keyup', searchByInput);\n"
				+ "        function searchByInput() {\n"
				+ "            var filter = $(\"#search\").val().toUpperCase();\n"
				+ "            var ul = $(\"#rootUL\");\n"
				+ "            var li = ul.find('li');\n"
				+ "            if (filter === \"\") {\n"
				+ "                li.each(function () {\n"
				+ "                    $(this).hide();\n"
				+ "                    $(this).find(' > span > i').addClass('icon-plus-sign').removeClass('icon-minus-sign').addClass(\"glyphicon-triangle-right\", \"glyphicon\").removeClass(\"glyphicon-triangle-bottom\", \"glyphicon\");\n"
				+ "                });\n"
				+ "                let children = ul.children();\n"
				+ "                ul.children().show();\n"
				+ "                return;\n" + "            }\n"
				+ "            li.each(function () {\n"
				+ "                var name = $(this).find(\"a\").text();\n"
				+ "                var size=$(this).parents(\"ul\").length;\n"
				+ "                if (size !== 0) {\n"
				+ "                    if (name.toUpperCase().indexOf(filter) >= 0) {\n"
				+ "                        $(this).show();\n"
				+ "                        $(this).find(' > span > i').addClass('icon-minus-sign').removeClass('icon-plus-sign').addClass(\"glyphicon-triangle-bottom\", \"glyphicon\").removeClass(\"glyphicon-triangle-right\", \"glyphicon\");\n"
				+ "                    } else {\n"
				+ "                        $(this).hide();\n"
				+ "                        $(this).find(' > span > i').addClass('icon-plus-sign').removeClass('icon-minus-sign').addClass(\"glyphicon-triangle-right\", \"glyphicon\").removeClass(\"glyphicon-triangle-bottom\", \"glyphicon\");\n"
				+ "                    }\n" + "                }\n"
				+ "            });\n" + "        }\n"
				+ "        $('.tree li:has(ul)').addClass('parent_li');\n"
				+ "        $('.tree li.parent_li > span').on('click', function (e) {\n"
				+ "            var children = $(this).parent('li.parent_li').find(' > ul > li');\n"
				+ "            if (children.is(\":visible\")) {\n"
				+ "                // 收起\n"
				+ "                children.hide('fast');\n"
				+ "                $(this).find(' > i').addClass('icon-plus-sign').removeClass('icon-minus-sign').addClass(\"glyphicon-triangle-right\", \"glyphicon\").removeClass(\"glyphicon-triangle-bottom\", \"glyphicon\");\n"
				+ "            } else {\n" + "                // 展开\n"
				+ "                children.show('fast');\n"
				+ "                $(this).find(' > i').addClass('icon-minus-sign').removeClass('icon-plus-sign').addClass(\"glyphicon-triangle-bottom\", \"glyphicon\").removeClass(\"glyphicon-triangle-right\", \"glyphicon\");\n"
				+ "            }\n" + "            e.stopPropagation();\n"
				+ "        });\n" + "    });");
		// 菜单中的标题
		HTMLElement treetitle = tree.div("treetitle");
		treetitle.attr("style",
				"padding-left: 40px;color: #626262;margin-bottom: 10px;");
		HTMLElement treeTitleSpan = treetitle.span();
		treeTitleSpan.text("Summary Report");
		treeTitleSpan.element("br");
		HTMLElement searchInput = treetitle.input("search");
		searchInput.attr("id", "search");
		searchInput.attr("placeholder", "查询内容");
		searchInput.attr("title", "请输入查询内容");
		searchInput.attr("style", "width: 80%");
		IBundleCoverage rootBundle = getRootBundle(this);
		Collection<IPackageCoverage> packages = rootBundle.getPackages();
		final HTMLElement firstUL = tree.ul("first_ul");
		firstUL.attr("id", "rootUL");
		firstUL.text("");
		// 首先循环所有包 创建一级菜单
		for (IPackageCoverage curPackage : packages) {
			final HTMLElement firstLI = firstUL.li("first_li");
			firstLI.text("");
			final HTMLElement first_span = firstLI.span("span_icon");
			first_span.text("");
			HTMLElement first_i = first_span
					.i("glyphicon glyphicon-triangle-right");
			first_i.text("");
			// HTMLElement firstI = firstSpan
			// .i("glyphicon glyphicon-triangle-right");
			// firstI.close();
			// String packageNamename = curPackage.getName();
			// 添加超链接
			final HTMLElement firstA = firstLI
					.a(getPackageName(curPackage) + "/index.html");
			firstA.attr("style", "font-size: xx-small;");
			firstA.text(getPackageName(curPackage));
			// 再循环所有类
			Collection<IClassCoverage> classes = curPackage.getClasses();
			final HTMLElement secondUL = firstLI.ul("second_ul");
			secondUL.text("");
			for (IClassCoverage curClass : classes) {
				if (!curClass.containsCode()) {
					continue;
				}

				final HTMLElement secondLI = secondUL.li("second_li");
				secondLI.attr("style", "display: none;");
				secondLI.text("");
				HTMLElement second_span = secondLI.span("span_icon");
				second_span.text("");
				HTMLElement second_i = second_span
						.i("glyphicon glyphicon-triangle-right");
				second_i.text("");
				// .i("glyphicon glyphicon-triangle-right");
				// HTMLElement secondI = secondSpan
				// .i("glyphicon glyphicon-triangle-right");
				// secondI.close();

				// String className = curClass.getName();
				// 添加超链接
				final HTMLElement secondA = secondLI
						.a(getPackageName(curPackage) + "/"
								+ getClassName(curClass) + ".html");
				secondA.attr("style", "font-size: xx-small;");
				secondA.text(getClassName(curClass));

				Collection<IMethodCoverage> methods = curClass.getMethods();
				// 循环所有方法
				final HTMLElement thirdUL = secondLI.ul("third_ul");
				thirdUL.text("");
				for (IMethodCoverage method : methods) {
					final HTMLElement thirdLI = thirdUL.li("third_li");
					thirdLI.attr("style", "display: none;");
					thirdLI.text("");
					HTMLElement third_span = thirdLI.span("span_icon");
					third_span.text("");
					// HTMLElement third_i = third_span
					// .i("glyphicon glyphicon-triangle-right");
					// third_i.text("");
					// HTMLElement thirdI = thirdSpan
					// .i("glyphicon glyphicon-triangle-right");
					// thirdI.close();
					// String methodName = method.getName();
					int firstLine = method.getFirstLine();
					String methodLink = getPackageName(curPackage) + "/"
							+ getClassName(curClass) + ".java.html";
					methodLink = firstLine != ISourceNode.UNKNOWN_LINE
							? methodLink + "#L" + firstLine
							: methodLink;
					// 添加超链接
					final HTMLElement thirdA = thirdLI.a(methodLink);
					thirdA.attr("style", "font-size: xx-small;");
					thirdA.text(getMethodName(curClass, method));
					// thirdLI.close();
				}
				// thirdUL.close();
				// secondLI.close();
			}
			// secondUL.close();
			// firstLI.close();
		}
		// firstUL.close();

	}

	private String getMethodName(IClassCoverage c, IMethodCoverage m) {
		return context.getLanguageNames().getMethodName(c.getName(),
				m.getName(), m.getDesc(), m.getSignature());
	}

	private String getClassName(IClassCoverage c) {
		return context.getLanguageNames().getClassName(c.getName(),
				c.getSignature(), c.getSuperName(), c.getInterfaceNames());
	}

	private String getPackageName(IPackageCoverage p) {
		return context.getLanguageNames().getPackageName(p.getName());
	}

	private void body(final HTMLElement body, String projectName,
			String appName, String envName, String timeStr,
			boolean isSourcePage, String location) throws IOException {
		body.attr("onload", getOnload());

		// todo 将页面分为两列，左侧为树形菜单，右侧为报告
		final HTMLElement left = body.div("left");
		creatMenuArea(left);
		left.close();
		// 右侧报告
		final HTMLElement right = body.div("right");

		// 标头添加面包屑
		// todo 后期改成 树形菜单跳转，去掉面包屑
		final HTMLElement navigation = right.div(Styles.BREADCRUMB);
		navigation.attr("id", "breadcrumb");
		// Session页
		// infoLinks(navigation.span(Styles.INFO));
		// 面包屑
		breadcrumb(navigation, folder);
		// 标题
		// body.h1().text(getLinkLabel());

		// 添加汇总数据
		if (!isSourcePage) {
			// 创建汇总区域
			final HTMLElement graphDiv = right.div("graph");
			graphDiv.attr("style",
					"border:2px solid lightgray;height:350px;width:100%;margin-top:30px;margin-bottom: 20px;");

			// 获取汇总数据
			Object summaryData = getSummaryNode();
			ICoverageNode summary = null;
			if (summaryData instanceof ICoverageNode) {
				summary = (ICoverageNode) summaryData;
			}
			// 创建汇总区域中标题、统计对象、数据等
			if (summary != null) {
				creatTitleArea(graphDiv, summary, projectName, appName, envName,
						timeStr, location);
			}

			// 创建汇总区域图表
			if (summary != null) {
				summary(graphDiv, summary, projectName, appName, envName);
				graphDiv.close();
			}
		}

		content(right);
		// 页脚 不需要可以去掉
		// footer(body);
	}

	/**
	 * Returns the onload handler for this page.
	 *
	 * @return handler or <code>null</code>
	 */
	protected String getOnload() {
		return null;
	}

	/**
	 * Inserts additional links on the top right corner.
	 *
	 * @param span
	 *            parent element
	 * @throws IOException
	 *             in case of IO problems with the report writer
	 */
	protected void infoLinks(final HTMLElement span) throws IOException {
		span.a(context.getSessionsPage(), folder);
	}

	private IBundleCoverage getRootBundle(final ReportPage page) {
		if (page.getResultNode() instanceof IBundleCoverage) {
			return (IBundleCoverage) page.getResultNode();
		} else {
			return page.getRootBundle(page.parent);
		}
	}

	private void breadcrumb(final HTMLElement div,
			final ReportOutputFolder base) throws IOException {
		breadcrumbParent(parent, div, base);
		div.span(getLinkStyle()).text(getLinkLabel());
	}

	private static void breadcrumbParent(final ReportPage page,
			final HTMLElement div, final ReportOutputFolder base)
			throws IOException {
		if (page != null) {
			breadcrumbParent(page.parent, div, base);
			div.a(page, base);
			div.text(" > ");
		}
	}

	private void footer(final HTMLElement body) throws IOException {
		final HTMLElement footer = body.div(Styles.FOOTER);
		final HTMLElement versioninfo = footer.span(Styles.RIGHT);
		versioninfo.text("Created with ");
		versioninfo.a(JaCoCo.HOMEURL).text("JaCoCo");
		versioninfo.text(" ");
		versioninfo.text(JaCoCo.VERSION);
		footer.text(context.getFooterText());
	}

	/**
	 * Specifies the local file name of this page.
	 *
	 * @return local file name
	 */
	protected abstract String getFileName();

	/**
	 * Creates the actual content of the page.
	 *
	 * @param body
	 *            body tag of the page
	 * @throws IOException
	 *             in case of IO problems with the report writer
	 */
	protected abstract void content(final HTMLElement body) throws IOException;

	// === ILinkable ===

	public final String getLink(final ReportOutputFolder base) {
		return folder.getLink(base, getFileName());
	}

}
