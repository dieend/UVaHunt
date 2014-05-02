package com.dieend.uvahunt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dieend.uvahunt.model.Submission;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.VerticalLabelPosition;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewSeries.Values;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.model.GraphViewDataInterface;
import com.jjoe64.graphview.renderer.BarGraphRenderer;
import com.jjoe64.graphview.renderer.HorizontalLabelRenderer;

public class SubmissionStatisticsFragment extends BaseFragment{

	SparseIntArray data = new SparseIntArray();
	String[] statistics = {"AC","PE","WA","TL","ML", "CE","RE","Other"};
	int[] verdict = {90,80,70,50,60,30,40,99};
	GraphView graph;
	List<GraphViewDataInterface> values = new ArrayList<GraphViewDataInterface>();
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {		
		BarGraphRenderer renderer = new BarGraphRenderer();
		renderer.setDrawValuesOnTop(true);
		renderer.setValuesOnTopColor(Color.BLACK);
		GraphViewSeries<GraphViewDataInterface> series = new GraphViewSeries<GraphViewDataInterface>(values, renderer);
		GraphViewSeriesStyle style = new GraphViewSeriesStyle();
		style.setValueDependentColor(new ValueDependentColor() {
			@Override
			public int get(GraphViewDataInterface data) {
				return Color.parseColor(Submission.verdictToColor(verdict[(int)data.getX()]));
			}
		});
		series.setHorizontalLabelRenderer(new HorizontalLabelRenderer() {
			final Paint paint = new Paint();
			@Override
			public void drawHorizontalLabels(Canvas canvas,
					Values<? extends GraphViewDataInterface> values, float border,
					float graphwidth, double diffX, float horstart, float canvasHeight,
					GraphViewStyle graphViewStyle) {
				paint.setTextAlign(Align.CENTER);
				paint.setAntiAlias(true);
				paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getActivity().getResources().getDisplayMetrics()));
				paint.setStrokeWidth(0);
				float barWidth = graphwidth / statistics.length;
				canvas.drawLine(horstart, canvasHeight - border, horstart, border, paint);
				for (int i = 0; i < statistics.length; i++) {
					paint.setColor(graphViewStyle.getGridColor());
					float x = (barWidth * i) + horstart;
		            paint.setColor(graphViewStyle.getHorizontalLabelsColor());
		            float locx = 1 + x + barWidth/2;
		            float locy = canvasHeight - 8 ;
		            canvas.drawText(statistics[i], locx, locy, paint);
				}
				
			}
		});
		series.setStyle(style);

		
		// TODO i18n
		graph = new GraphView(getActivity(), "Submission Statistics", VerticalLabelPosition.LEFT);
		graph.setScrollable(false);
		graph.addSeries(series);
				
		return graph;
	}
	public void updateSubmission(Map<Integer, Submission> submissions) {
		data.clear();
		for (Submission submission: submissions.values()) {
			Integer old = data.get(submission.getVerdict());
			int val = old == null? 0:old;
			data.put(submission.getVerdict(), val + 1 );
//			data.put(submission.getLangReadable(), data.get(submission.getLangReadable()+1));
		}
		int total = 0;
		values.clear();
		for (int i=0; i<verdict.length-1; i++) {
			Integer valueI = data.get(verdict[i]);
			int value = valueI == null? 0:valueI;
			total += value;
			values.add(new GraphView.GraphViewData(i, value));
		}
		// other
		
		values.add(new GraphView.GraphViewData(statistics.length-1, submissions.size()-total));
		executeWhenViewReady(new ViewTask() {
			@Override
			public void run() {
				graph.redrawAll();
			}
		});
	}
	
}
