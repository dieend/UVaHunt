package com.dieend.uvahunt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dieend.uvahunt.model.DBManager;
import com.dieend.uvahunt.model.Problem;
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

public class SolvedProblemLevelFragment extends BaseFragment{
	GraphView graph;
	int[] data = new int[10];
	String[] color = {"#00E51D", "#18E000", "#4CDC00", "#7ED800", "#AED400", "#CFC300", "#CB8E00", "#C75C00","#C32C00","#BF0001"};
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
				return Color.parseColor(color[(int)data.getX()]);
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
				float barWidth = graphwidth / 10;
				canvas.drawLine(horstart, canvasHeight - border, horstart, border, paint);
				for (int i = 0; i < 10; i++) {
					paint.setColor(graphViewStyle.getGridColor());
					float x = (barWidth * i) + horstart;
		            paint.setColor(graphViewStyle.getHorizontalLabelsColor());
		            float locx = 1 + x + barWidth/2;
		            float locy = canvasHeight - 8 ;
		            canvas.drawText("" + (i+1), locx, locy, paint);
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
	public void updateProblem() {
		Collection<Integer> problemIds = Problem.solvedProblems();
		for (int i=0; i<10; i++) {
			data[i] = 0;
		}
		for (Integer i: problemIds) {
			Problem p = DBManager.$().getProblemsById(i);
			if (p.getLevel() == 0) {
				Log.d("aaa", p.getTitle() + " " + p.getNumber());
			}
			data[p.getLevel()]++;
		}
		values.clear();
		for (int i=0; i<10; i++) {
			values.add(new GraphView.GraphViewData(i, data[i]));
		}
	}

}
